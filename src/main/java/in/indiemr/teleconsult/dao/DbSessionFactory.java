package in.indiemr.teleconsult.dao;


import java.util.function.Consumer;
import java.util.function.Function;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;
import org.hibernate.Transaction;

@Component
public class DbSessionFactory {
    
    private final SessionFactory sessionFactory;

    public DbSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Session openSession() {
        return sessionFactory.openSession();
    }

    // Read only, no transaction
    public <T> T inSession(Function<Session, T> work) {
        try(Session session = openSession()) {
            return work.apply(session);
        }
    }

    // Write work with commit or rollback
    public void inTransaction(Consumer<Session> work) {
        Transaction tx = null;
        try(Session session = openSession()) {
            tx = session.beginTransaction();
            work.accept(session);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public <T> T inTransactionReturn(Function<Session, T> work) {
        Transaction tx = null;
        try (Session session = openSession()) {
            tx = session.beginTransaction();
            T result = work.apply(session);
            tx.commit();
            return result;
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}
