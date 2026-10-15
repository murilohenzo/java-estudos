package br.com.murilo.javaee.ejb;

import jakarta.ejb.*;
import jakarta.annotation.Resource;
import jakarta.transaction.TransactionSynchronizationRegistry;

@Stateless
public class TransacaoProbeEJB {
    @Resource
    private TransactionSynchronizationRegistry registro;

    public TransacaoProbeEJB() {}

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public boolean required() { return ativa(); }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public boolean requiresNew() { return ativa(); }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public boolean mandatory() { return ativa(); }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public boolean supports() { return ativa(); }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public boolean notSupported() { return ativa(); }

    @TransactionAttribute(TransactionAttributeType.NEVER)
    public boolean never() { return ativa(); }

    private boolean ativa() { return registro.getTransactionKey() != null; }
}
