package in.indiemr.teleconsult.dao;

import in.indiemr.teleconsult.model.ExternalResourceMapping;
import in.indiemr.teleconsult.model.TeleconsultLink;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDateTime;

@Repository
public class TeleconsultLinkDao {

    private final DbSessionFactory db;

    public TeleconsultLinkDao(DbSessionFactory db) {
        this.db = db;
    }

    public TeleconsultLink save(TeleconsultLink link) {
        return db.inTransactionReturn(session -> {
            LocalDateTime now = LocalDateTime.now();
            link.setCreatedAt(now);
            link.setUpdatedAt(now);
            if (link.getStatus() == null) {
                link.setStatus(TeleconsultLink.STATUS_CREATED);
            }
            session.persist(link);
            return link;
        });
    }

    public Optional<TeleconsultLink> findByToken(String token) {
        return db.inSession(session -> 
            session.createQuery(
                """
                    from TeleconsultLink l
                    join fetch l.oauthAccount a
                    where l.token = :token and l.voided = false
                """,
                TeleconsultLink.class
            )
            .setParameter("token", token)
            .uniqueResultOptional()
        );
    }

    public void markStatus(String token, String status) {
        db.inTransaction(session -> {
            TeleconsultLink link = session.createQuery(
                "from TeleconsultLink l where l.token = :token",
                TeleconsultLink.class)
            .setParameter("token", token)
            .uniqueResult();
            if (link != null) {
                link.setStatus(status);
                link.setUpdatedAt(LocalDateTime.now());
                session.merge(link);
            }
        });
    }

    // public void voidByAppointmentUuid(String appointmentUuid) {
    //     db.inTransaction(session ->
    //         session.createMutationQuery("""
    //             update TeleconsultLink l
    //             set l.voided = true,
    //                 l.status = :status,
    //                 l.updatedAt = :now
    //             where l.voided = false
    //               and l.externalResourceMapping.internalResourceType = :type
    //               and l.externalResourceMapping.internalResourceUuid = :uuid
    //             """)
    //             .setParameter("type", ExternalResourceMapping.INTERNAL_APPOINTMENT)
    //             .setParameter("uuid", appointmentUuid)
    //             .setParameter("status", TeleconsultLink.STATUS_EXPIRED)
    //             .setParameter("now", LocalDateTime.now())
    //             .executeUpdate()
    //     );
    // }

    public void voidByAppointmentUuid(String appointmentUuid) {
        db.inTransaction(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaUpdate<TeleconsultLink> updateQuery = cb.createCriteriaUpdate(TeleconsultLink.class);
            Root<TeleconsultLink> link = updateQuery.from(TeleconsultLink.class);

            LocalDateTime now = LocalDateTime.now();

            updateQuery.set(link.get("voided"), true);
            updateQuery.set(link.get("status"), TeleconsultLink.STATUS_EXPIRED);
            updateQuery.set(link.get("updatedAt"), now);

            updateQuery.where(
                cb.isFalse(link.get("voided")),
                cb.equal(
                    link.get("externalResourceMapping").get("internalResourceType"), 
                    ExternalResourceMapping.INTERNAL_APPOINTMENT
                ),
                cb.equal(
                    link.get("externalResourceMapping").get("internalResourceUuid"),
                    appointmentUuid
                )
            );

            session.createMutationQuery(updateQuery).executeUpdate();
        });
    }
}