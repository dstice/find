package com.hp.autonomy.frontend.configuration;

import org.springframework.beans.factory.FactoryBean;

/**
 * Generates the password for encrypting the PostgresPassword
 */
public class TextEncryptorPasswordFactory implements FactoryBean<String> {

    // exception declared in external interface
    @SuppressWarnings("ProhibitedExceptionDeclared")
    @Override
    public String getObject() throws Exception {
        // don't change this or existing config files with encrypted text will stop working
        return "sdfjnhejsRU£HR£$uwhr843y5432rjsadfjsehR$HWENFU£5y472345792348yJGNEO";
    }

    @Override
    public Class<?> getObjectType() {
        return String.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}