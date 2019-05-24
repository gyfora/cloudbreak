package com.sequenceiq.cloudbreak.cm;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cloudera.api.swagger.ClouderaManagerResourceApi;
import com.cloudera.api.swagger.MgmtRoleConfigGroupsResourceApi;
import com.cloudera.api.swagger.MgmtRolesResourceApi;
import com.cloudera.api.swagger.MgmtServiceResourceApi;
import com.cloudera.api.swagger.client.ApiClient;
import com.cloudera.api.swagger.client.ApiException;
import com.cloudera.api.swagger.model.ApiCommand;
import com.cloudera.api.swagger.model.ApiConfig;
import com.cloudera.api.swagger.model.ApiConfigList;
import com.cloudera.api.swagger.model.ApiHostRef;
import com.cloudera.api.swagger.model.ApiRole;
import com.cloudera.api.swagger.model.ApiRoleList;
import com.cloudera.api.swagger.model.ApiService;
import com.cloudera.api.swagger.model.ApiServiceState;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.sequenceiq.cloudbreak.api.endpoint.v4.database.base.DatabaseType;
import com.sequenceiq.cloudbreak.cm.client.DataView;
import com.sequenceiq.cloudbreak.cm.database.DatabaseProperties;
import com.sequenceiq.cloudbreak.cm.polling.ClouderaManagerPollingServiceProvider;
import com.sequenceiq.cloudbreak.domain.RDSConfig;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.util.DatabaseCommon;

/**
 * Sets up management services for the given Cloudera Manager server.
 */
@Service
public class ClouderaManagerMgmtSetupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClouderaManagerMgmtSetupService.class);

    private static final String MGMT_SERVICE = "MGMT";

    private static final String ACTIVITYMONITOR = "ACTIVITYMONITOR";

    private static final String REPORTSMANAGER = "REPORTSMANAGER";

    // This map contains the roles and their internal config name that's necessary for setting database config values
    private static final Map<String, String> ROLE_TYPE_TO_INTERNAL_NAME = ImmutableMap.of(
            ACTIVITYMONITOR, "firehose",
            REPORTSMANAGER, "headlamp");

    private static final String TELEMETRYPUBLISHER = "TELEMETRYPUBLISHER";

    private static final String TELEMETRY_MASTER = "telemetry_master";

    private static final String TELEMETRY_WA = "telemetry_wa";

    private static final String TELEMETRY_COLLECT_JOB_LOGS = "telemetry_collect_job_logs";

    private static final String TELEMETRY_ALTUS_ACCOUNT = "telemetry_altus_account";

    private static final List<String> BLACKLISTED_ROLE_TYPES = ImmutableList.of(
            TELEMETRYPUBLISHER, "NAVIGATOR", "NAVIGATORMETASERVER");

    @Inject
    private ClouderaManagerPollingServiceProvider clouderaManagerPollingServiceProvider;

    @Inject
    private ClouderaManagerLicenseService licenseService;

    @Inject
    private DatabaseCommon databaseCommon;

    /**
     * Sets up the management services using the given Cloudera Manager client.
     *
     * @param stack      the stack
     * @param client     the CM API client
     * @param cmHostRef  reference to the CM host
     * @param rdsConfigs the set of all database configs
     * @throws ApiException if there's a problem setting up management services
     */
    public void setupMgmtServices(Stack stack, ApiClient client, ApiHostRef cmHostRef, Set<RDSConfig> rdsConfigs)
            throws ApiException {
        licenseService.beginTrialIfNeeded(stack.getCreator(), client);

        ClouderaManagerResourceApi clouderaManagerResourceApi = new ClouderaManagerResourceApi(client);
        MgmtServiceResourceApi mgmtServiceResourceApi = new MgmtServiceResourceApi(client);
        MgmtRolesResourceApi mgmtRolesResourceApi = new MgmtRolesResourceApi(client);

        ApiService mgmtService = new ApiService();
        mgmtService.setName(MGMT_SERVICE);
        mgmtService.setType(MGMT_SERVICE);

        setupCMS(mgmtServiceResourceApi, mgmtService);

        ApiRoleList mgmtRoles = new ApiRoleList();
        List<String> roleTypes = mgmtServiceResourceApi.listRoleTypes().getItems();
        for (String roleType : roleTypes) {
            if (!BLACKLISTED_ROLE_TYPES.contains(roleType)) {
                ApiRole apiRole = new ApiRole();
                apiRole.setName(roleType);
                apiRole.setType(roleType);
                apiRole.setHostRef(cmHostRef);
                mgmtRoles.addItemsItem(apiRole);
            }
        }

        // Add TELEMETRYPUBLISHER if configs are set up properly
        // We have to do this because we're unable to take advantage of mgmtServiceResourceApi.autoAssignRoles,
        // which would have this logic baked in server side
        Map<String, String> configs = clouderaManagerResourceApi.getConfig(DataView.SUMMARY.name()).getItems().stream()
                .collect(Collectors.toMap(ApiConfig::getName, ApiConfig::getValue));
        if (Boolean.parseBoolean(configs.getOrDefault(TELEMETRY_MASTER, "false"))
                && Boolean.parseBoolean(configs.getOrDefault(TELEMETRY_WA, "false"))
                && Boolean.parseBoolean(configs.getOrDefault(TELEMETRY_COLLECT_JOB_LOGS, "false"))
                && configs.containsKey(TELEMETRY_ALTUS_ACCOUNT)) {
            ApiRole telemetryPublisher = new ApiRole();
            telemetryPublisher.setName(TELEMETRYPUBLISHER);
            telemetryPublisher.setType(TELEMETRYPUBLISHER);
            telemetryPublisher.setHostRef(cmHostRef);
            mgmtRoles.addItemsItem(telemetryPublisher);
        }
        createMgmtRoles(mgmtRolesResourceApi, mgmtRoles);
        createMgmtDatabases(client, rdsConfigs);
        startMgmtServices(stack, client, mgmtServiceResourceApi);
    }

    private void createMgmtRoles(MgmtRolesResourceApi mgmtRolesResourceApi, ApiRoleList mgmtRoles) throws ApiException {
        try {
            mgmtRolesResourceApi.createRoles(mgmtRoles);
        } catch (ApiException ex) {
            if (!ex.getResponseBody().contains("The maximum number of instances")) {
                throw ex;
            }
        }
    }

    private void startMgmtServices(Stack stack, ApiClient client, MgmtServiceResourceApi mgmtServiceResourceApi) throws ApiException {
        ApiService mgmtService = mgmtServiceResourceApi.readService("SUMMARY");
        Optional<ApiCommand> startCommand = Optional.empty();
        if (mgmtService.getServiceState() == ApiServiceState.STARTING) {
            startCommand = mgmtServiceResourceApi.listActiveCommands("SUMMARY").getItems()
                    .stream().filter(c -> c.getName().equals("Start")).findFirst();
        } else if (mgmtService.getServiceState() != ApiServiceState.STARTED) {
            startCommand = Optional.of(mgmtServiceResourceApi.startCommand());
        }
        startCommand.ifPresent(sc -> clouderaManagerPollingServiceProvider.startManagementServicePollingService(stack, client, sc.getId()));
    }

    private void setupCMS(MgmtServiceResourceApi mgmtServiceResourceApi, ApiService mgmtService) throws ApiException {
        try {
            mgmtServiceResourceApi.setupCMS(mgmtService);
        } catch (ApiException ex) {
            if (!ex.getResponseBody().contains("CMS instance already exists.")) {
                throw ex;
            }
        }
    }

    private void createMgmtDatabases(ApiClient client, Set<RDSConfig> rdsConfigs) throws ApiException {
        MgmtRolesResourceApi mgmtRolesResourceApi = new MgmtRolesResourceApi(client);

        List<ApiRole> installedRoles = mgmtRolesResourceApi.readRoles().getItems();

        for (ApiRole role : installedRoles) {
            String roleType = role.getType();
            Optional<RDSConfig> rdsConfig = Optional.empty();
            switch (roleType) {
                case ACTIVITYMONITOR:
                    rdsConfig =
                            findDbConfig(DatabaseType.CLOUDERA_MANAGER_MANAGEMENT_SERVICE_ACTIVITY_MONITOR, rdsConfigs);
                    break;
                case REPORTSMANAGER:
                    rdsConfig =
                            findDbConfig(DatabaseType.CLOUDERA_MANAGER_MANAGEMENT_SERVICE_REPORTS_MANAGER, rdsConfigs);
                    break;
                default:
                    break;
            }

            if (rdsConfig.isPresent()) {
                updateDatabaseForComponent(client, roleType, rdsConfig.get());
            }
        }
    }

    private Optional<RDSConfig> findDbConfig(DatabaseType databaseType, Set<RDSConfig> rdsConfigs) {
        return rdsConfigs.stream()
                .filter(dbConfig -> databaseType.name()
                        .equals(dbConfig.getType()))
                .findFirst();
    }

    private void updateDatabaseForComponent(ApiClient client, String roleType, RDSConfig rdsConfig)
            throws ApiException {
        MgmtRoleConfigGroupsResourceApi mgmtRoleConfigGroupsResourceApi = new MgmtRoleConfigGroupsResourceApi(client);
        String internalRoleTypeName = ROLE_TYPE_TO_INTERNAL_NAME.get(roleType);
        mgmtRoleConfigGroupsResourceApi.updateConfig(getBaseRoleConfigGroupName(roleType),
                "Adding database settings",
                buildApiConfigList(internalRoleTypeName, rdsConfig));
    }

    /**
     * Generate the default internal name of the mgmt RCGs as defined by CM.
     */
    @VisibleForTesting
    String getBaseRoleConfigGroupName(String configuration) {
        requireNonNull(configuration);
        return Joiner.on("-").join("MGMT", configuration, "BASE");
    }

    /**
     * Builds an API config list given the database configuration. The prefix is added onto
     * the beginning of the database config names to get the accurate database configuration key
     * to assign a value to.
     *
     * @param prefix    the prefix to use to generate database config keys
     * @param rdsConfig the database configuration
     * @return an API config list
     */
    @VisibleForTesting
    ApiConfigList buildApiConfigList(String prefix, RDSConfig rdsConfig) {
        String connectionUrl = rdsConfig.getConnectionURL();

        // We can safely retrieve these values since they should have passed validation
        DatabaseCommon.JdbcConnectionUrlFields connectionUrlFields = databaseCommon.parseJdbcConnectionUrl(connectionUrl);
        String type = connectionUrlFields.getVendorDriverId();
        String hostAndPort = connectionUrlFields.getHostAndPort();
        String databaseName = connectionUrlFields.getDatabase().get();

        ApiConfigList result = new ApiConfigList();
        result.addItemsItem(makeApiConfig(getDatabaseApiConfigName(prefix, DatabaseProperties.TYPE_PROPERTY),
                type));
        result.addItemsItem(makeApiConfig(getDatabaseApiConfigName(prefix, DatabaseProperties.HOST_PROPERTY),
                hostAndPort));
        result.addItemsItem(makeApiConfig(getDatabaseApiConfigName(prefix, DatabaseProperties.NAME_PROPERTY),
                databaseName));
        result.addItemsItem(makeApiConfig(getDatabaseApiConfigName(prefix, DatabaseProperties.USER_PROPERTY),
                rdsConfig.getConnectionUserName()));
        result.addItemsItem(makeApiConfig(getDatabaseApiConfigName(prefix, DatabaseProperties.PASSWORD_PROPERTY),
                rdsConfig.getConnectionPassword()));

        return result;
    }

    private String getDatabaseApiConfigName(String prefix, String propertyName) {
        return String.format("%s_database_%s", prefix, propertyName);
    }

    private ApiConfig makeApiConfig(String name, String value) {
        ApiConfig apiConfig = new ApiConfig();

        apiConfig.setName(name);
        apiConfig.setValue(value);

        return apiConfig;
    }
}
