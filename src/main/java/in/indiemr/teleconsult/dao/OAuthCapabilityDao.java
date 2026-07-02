package in.indiemr.teleconsult.dao;

import in.indiemr.teleconsult.model.OAuthCapability;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class OAuthCapabilityDao {

    private final DbSessionFactory db;

    public OAuthCapabilityDao(DbSessionFactory db) {
        this.db = db;
    }

    public Optional<OAuthCapability> findByCode(String code) {
        return db.inSession(session ->
            session.createQuery(
                "from OAuthCapability c where c.code = :code",
                OAuthCapability.class)
            .setParameter("code", code)
            .uniqueResultOptional()
        );
    }

    public List<OAuthCapability> findByCodes(List<String> codes) {
        return db.inSession(session ->
            session.createQuery(
                "from OAuthCapability c where c.code in :codes",
                OAuthCapability.class)
            .setParameterList("codes", codes)
            .list()
        );
    }
}