package com.sequenceiq.environment.environment.experience.common;

import com.sequenceiq.environment.environment.domain.Environment;
import com.sequenceiq.environment.environment.experience.Experience;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.sequenceiq.cloudbreak.util.ConditionBasedEvaluatorUtil.throwIfTrue;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Service
public class XService implements Experience {

    private static final Logger LOGGER = LoggerFactory.getLogger(XService.class);

    private static final String DEFAULT_EXPERIENCE_PROTOCOL = "https";

    private final CommonExperienceConnectorService experienceConnectorService;

    private final Map<String, CommonExperience> configuredExperiences;

    private final CommonExperienceValidator experienceValidator;

    private final String experienceProtocol;

    private final String pathPostfix;

    public XService(@Value("${xp.protocol:https}") String experienceProtocol, @Value("${xp.path.postfix}") String pathPostfix,
                    CommonExperienceConnectorService experienceConnectorService, XPServices experienceProvider, CommonExperienceValidator experienceValidator) {
        this.experienceValidator = experienceValidator;
        this.configuredExperiences = identifyConfiguredExperiences(experienceProvider);
        this.pathPostfix = StringUtils.isEmpty(pathPostfix) ? "" : pathPostfix;
        LOGGER.debug("Experience checking postfix set to: {}", this.pathPostfix);
        this.experienceConnectorService = experienceConnectorService;
        this.experienceProtocol = StringUtils.isEmpty(experienceProtocol) ? DEFAULT_EXPERIENCE_PROTOCOL : experienceProtocol;
        LOGGER.debug("Experience connection protocol set to: {}", this.experienceProtocol);
    }

    @Override
    public boolean hasExistingClusterForEnvironment(Environment environment) {
        Set<String> activeExperienceNames = environmentHasActiveExperience(environment.getResourceCrn());
        if (activeExperienceNames.size() > 0) {
            String combinedNames = String.join(",", activeExperienceNames);
            LOGGER.info("The following experiences has connected to this env: [env: {}, experience(s): {}]", environment.getName(), combinedNames);
            return true;
        }
        return false;
    }

    /**
     * Checks all the configured experiences for any existing workspace which has a connection with the given environment.
     * If so, it will return the set of the names of the given experience.
     *
     * @param environmentCrn the resource crn of the environment. It must not be null or empty.
     * @return the name of the experiences which has an active workspace for the given environment.
     * @throws IllegalArgumentException if environmentCrn is null or empty
     */
    private Set<String> environmentHasActiveExperience(@NotNull String environmentCrn) {
        throwIfTrue(StringUtils.isEmpty(environmentCrn), () -> new IllegalArgumentException("Unable to check environment - experience relation, since the " +
                "given environment crn is null or empty!"));
        Set<String> affectedExperiences;
        affectedExperiences = configuredExperiences
                .entrySet()
                .stream()
                .filter(this::isExperienceConfigured)
                .map(xp -> isExperienceActiveForEnvironment(xp.getKey(), xp.getValue(), environmentCrn))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toSet());
        return affectedExperiences;
    }

    private Optional<String> isExperienceActiveForEnvironment(String experienceName, CommonExperience xp, String environmentCrn) {
        LOGGER.debug("Checking whether the environment (crn: {}) has an active experience (name: {}) or not.", environmentCrn, experienceName);
        Set<String> entries = collectExperienceEntryNamesWhenItHasActiveWorkspaceForEnv(xp, environmentCrn);
        if (!entries.isEmpty()) {
            String entryNames = String.join(",", entries);
            LOGGER.info("The following experience ({}) has an active entry for the given environment! [entries: {}, environmentCrn: {}]",
                    experienceName, entryNames, environmentCrn);
            return Optional.of(experienceName);
        }
        return Optional.empty();
    }

    private Set<String> collectExperienceEntryNamesWhenItHasActiveWorkspaceForEnv(CommonExperience xp, String envCrn) {
        String pathToExperience = experienceProtocol + "://" + xp.getPathPrefix() + ":" + xp.getPort() + xp.getPathInfix() + pathPostfix;
        return experienceConnectorService.getWorkspaceNamesConnectedToEnv(pathToExperience, envCrn);
    }

    private Map<String, CommonExperience> identifyConfiguredExperiences(XPServices experienceProvider) {
        Map<String, CommonExperience> experiences = experienceProvider.getExperiences()
                .entrySet()
                .stream()
                .filter(this::isExperienceConfigured)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (experiences.isEmpty()) {
            LOGGER.info("There are no - properly - configured experience endpoints in environment service! If you would like to check them, specify them" +
                    " in the application.yml!");
            return emptyMap();
        } else {
            String names = String.join(", ", new HashSet<>(experiences.keySet()));
            LOGGER.info("The following experience(s) have given for environment service: {}", names);
            return experiences;
        }
    }

    private boolean isExperienceConfigured(Map.Entry<String, CommonExperience> xp) {
        boolean filled = experienceValidator.isExperienceFilled(xp.getValue());
        if (!filled) {
            LOGGER.debug("The following experience has not filled properly: {}", xp.getKey());
        }
        return filled;
    }

}