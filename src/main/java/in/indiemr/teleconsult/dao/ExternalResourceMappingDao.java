package in.indiemr.teleconsult.dao;

import in.indiemr.teleconsult.model.ExternalResourceMapping;
import in.indiemr.teleconsult.model.OAuthAccount;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class ExternalResourceMappingDao {
    private final DbSessionFactory db;

    public ExternalResourceMappingDao(DbSessionFactory db) {
        this.db = db;
    }

    public ExternalResourceMapping save(ExternalResourceMapping mapping) {
        return db.inTransactionReturn(session -> {
            if (mapping.getCreatedAt() == null) {
                mapping.setCreatedAt(LocalDateTime.now());
            }
            OAuthAccount account = session.get(OAuthAccount.class, mapping.getOauthAccount().getId());
            mapping.setOauthAccount(account);
            session.persist(mapping);
            return mapping;
        });
    }

    public List<ExternalResourceMapping> findByInternalResource(
        String internalResourceType, String internalResourceUuid
    ) {
        return db.inSession(session -> 
            session.createQuery(
                """
                    from ExternalResourceMapping m
                    join fetch m.oauthAccount a
                    join fetch a.oauthProvider p
                    where m.internalResourceType = :type
                        and m.internalResourceUuid = :uuid
                        and m.voided = false
                        """
                ,
                ExternalResourceMapping.class
            )
            .setParameter("type", internalResourceType)
            .setParameter("uuid", internalResourceUuid)
            .list()
        );
    }

    public void voidByInternalResource(String internalResourceType, String internalResourceUuid) {
        db.inTransaction(session ->
            session.createMutationQuery("""
                update ExternalResourceMapping m
                set m.voided = true
                where m.internalResourceType = :type
                  and m.internalResourceUuid = :uuid
                  and m.voided = false
                """)
            .setParameter("type", internalResourceType)
            .setParameter("uuid", internalResourceUuid)
            .executeUpdate()
        );
    }
}
