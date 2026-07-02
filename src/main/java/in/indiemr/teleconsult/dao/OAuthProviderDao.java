package in.indiemr.teleconsult.dao;

import in.indiemr.teleconsult.model.OAuthProvider;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class OAuthProviderDao {
    private final DbSessionFactory db;

    public OAuthProviderDao(DbSessionFactory db) {
        this.db = db;
    }

    public Optional<OAuthProvider> findEnabledByCode(String code) {
        return db.inSession(session ->
            session.createQuery(
                "from OAuthProvider p where p.code = :code and p.enabled = true",
                OAuthProvider.class
            ).setParameter("code", code)
            .uniqueResultOptional()
        );
    }

    public Optional<OAuthProvider> findByCode(String code) {
        return db.inSession(session -> 
            session.createQuery(
                "from OAuthProvider p where p.code = :code",
                OAuthProvider.class
            ).setParameter("coded", code)
            .uniqueResultOptional()
        );
    }
}
