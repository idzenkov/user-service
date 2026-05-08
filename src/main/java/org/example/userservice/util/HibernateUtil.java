package org.example.userservice.util;


import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateUtil {


    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // Загружаем конфигурацию из hibernate.cfg.xml
            Configuration configuration = new Configuration().configure();
            // Можно явно добавить аннотированный класс (но configure() прочитает отображения из XML,
            // лучше добавить программно, чтобы избежать ошибок)
            configuration.addAnnotatedClass(org.example.userservice.entity.User.class);
            return configuration.buildSessionFactory();
        } catch (Throwable ex) {
            logger.error("Initial SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        getSessionFactory().close();
        logger.info("SessionFactory closed.");
    }
}
