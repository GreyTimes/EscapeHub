package com.escapehub.escapehub.config;

import com.escapehub.escapehub.mission.definition.importer.MissionDefinitionDocument;
import com.escapehub.escapehub.mission.definition.importer.MissionImportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Component
@Profile("dev")
public class DevelopmentDataInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final JsonMapper jsonMapper;
    private final MissionImportService missionImportService;
    private final UUID developmentUserId;
    private final String developmentUserDisplayName;
    private final Resource missionResource;

    public DevelopmentDataInitializer(
            JdbcTemplate jdbcTemplate,
            JsonMapper jsonMapper,
            MissionImportService missionImportService,
            @Value("${app.dev.user-id}") UUID developmentUserId,
            @Value("${app.dev.user-display-name:Local Developer}") String developmentUserDisplayName,
            @Value("${app.dev.mission-resource}") Resource missionResource
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonMapper = jsonMapper;
        this.missionImportService = missionImportService;
        this.developmentUserId = developmentUserId;
        this.developmentUserDisplayName = developmentUserDisplayName;
        this.missionResource = missionResource;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        seedDevelopmentUser();

        String sourceJson;
        try (var input = missionResource.getInputStream()) {
            sourceJson = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        MissionDefinitionDocument definition = jsonMapper.readValue(sourceJson, MissionDefinitionDocument.class);
        var importedVersion = missionImportService.importMission(definition, sourceJson);

        System.out.println("[dev] Sample mission loaded: "
                + definition.mission().key() + " (version "
                + definition.mission().versionNumber() + ", id " + importedVersion.getId() + ")");
    }

    private void seedDevelopmentUser() {
        // PostgreSQL upsert keeps the configured VITE_DEV_USER_ID stable and avoids
        // changing the application's normal UUID generation strategy.
        jdbcTemplate.update("""
                INSERT INTO users (id, display_name, role, created_at)
                VALUES (?, ?, 'PLAYER', ?)
                ON CONFLICT (id) DO UPDATE
                SET display_name = EXCLUDED.display_name
                """,
                developmentUserId,
                developmentUserDisplayName,
                Timestamp.from(Instant.now())
        );
    }
}
