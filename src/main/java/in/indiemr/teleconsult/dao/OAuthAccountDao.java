package in.indiemr.teleconsult.dao;

import in.indiemr.teleconsult.model.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

@Repository
public class OAuthAccountDao {
    private final DbSessionFactory db;
    public OAuthAccountDao(DbSessionFactory db) {
        this.db = db;
    }

    public Optional<OAuthAccount> findById(Long id) {
        return db.inSession(session -> 
            session.createQuery(
                "from OAuthAccount a join fetch a.oauthProvider where a.id = :id and a.voided = false",
                OAuthAccount.class
            ).setParameter("id", id)
            .uniqueResultOptional()
        );
    }

    public Optional<OAuthAccount> findByProviderUuidAndProviderCode(
        String providerUUid,
        String oauthProviderCode
    ) {
        return db.inSession(session -> 
            session.createQuery(
                """
                    from OAuthAccount a
                    join fetch a.oauthProvider p
                    where a.providerUuid = :providerUuid
                        and p.code = :providerCode
                        and a.voided = false
                        """
            ,OAuthAccount.class)
            .setParameter("providerUuid", providerUUid)
            .setParameter("providerCode", oauthProviderCode)
            .uniqueResultOptional()
        );
    }

    public List<OAuthAccount> findAllByProviderUuid(String providerUuid) {
        return db.inSession(session -> 
            session.createQuery("""
                    from OAuthAccount a
                    join fetch a.oauthProvider p
                    where a.providerUuid = :providerUuid
                        and a.voided = false
                    """, OAuthAccount.class)
                .setParameter("providerUuid", providerUuid)
                .list()
        );
    }

    public OAuthAccount save(OAuthAccount account) {
        return db.inTransactionReturn(session -> persistOrMerge(session, account));
    }

    /**
     * Upsert account + replace capability links in one transaction.
     * capabilityCodes e.g. CALENDAR, VIDEO_MEETING, EMAIL
     */
    public OAuthAccount saveWithCapabilities(OAuthAccount account, List<String> capabilityCodes) {
        return db.inTransactionReturn(session -> {
            OAuthAccount managed = persistOrMerge(session, account);

            // clear old capability rows
            session.createMutationQuery(
                "delete from OAuthAccountCapability c where c.oauthAccount.id = :accountId")
                .setParameter("accountId", managed.getId())
                .executeUpdate();

            managed.getCapabilities().clear();

            for (String code : capabilityCodes) {
                OAuthCapability cap = session.createQuery(
                    "from OAuthCapability c where c.code = :code",
                    OAuthCapability.class)
                    .setParameter("code", code)
                    .uniqueResultOptional()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown capability: " + code));

                OAuthAccountCapability link = new OAuthAccountCapability();
                link.setOauthAccount(managed);
                link.setCapability(cap);
                managed.getCapabilities().add(link);
                session.persist(link);
            }

            session.merge(managed);
            return managed;
        });
    }

    public void markRevoked(Long accountId) {
        db.inTransaction(session -> {
            OAuthAccount account = session.get(OAuthAccount.class, accountId);
            if (account != null) {
                account.setStatus(OAuthAccount.STATUS_REVOKED);
                account.setUpdatedAt(LocalDateTime.now());
                session.merge(account);
            }
        });
    }

    private OAuthAccount persistOrMerge(Session session, OAuthAccount account){
        LocalDateTime now = LocalDateTime.now();
        if (account.getId() == null) {
            account.setCreatedAt(now);
        }
        account.setUpdatedAt(now);
        if (account.getStatus() == null) {
            account.setStatus(OAuthAccount.STATUS_ACTIVE);
        }

        OAuthProvider provider = session.createQuery(
            "from OAuthProvider p where p.id = :id",
            OAuthProvider.class
        )
        .setParameter("id", account.getOauthProvider().getId())
        .uniqueResult();

        account.setOauthProvider(provider);
        if (account.getId() == null) {
            session.persist(account);
            return account;
        }
        return session.merge(account);
    }

}
