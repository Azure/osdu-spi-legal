package org.opengroup.osdu.legal.tags.dataaccess;

import com.google.rpc.Code;
import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;
import org.opengroup.osdu.core.common.model.legal.PersistenceException;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

import java.time.Duration;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

public class ResilientLegalTagRepository extends LegalTagRepositoryWrapper {

    private static final String CONTRACT_DATASTORE = "ContractDatastore";
    public static final int WAIT_TIME_MILLI = 1500;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public ResilientLegalTagRepository(ILegalTagRepository wrapped){
       this(wrapped, WAIT_TIME_MILLI);
    }
    ResilientLegalTagRepository(ILegalTagRepository wrapped, int initialWaitTimeMilli){
        super(wrapped);
        this.retry = createDatastoreRetryConfig(initialWaitTimeMilli);
        this.circuitBreaker = createCircuitBreaker();
    }

    @Override
    public Long create(LegalTag legalTag) throws PersistenceException {
        Function<LegalTag, Long> func = Retry.decorateFunction(retry, super::create);
        func = CircuitBreaker.decorateFunction(circuitBreaker, func);
        return func.apply(legalTag);
    }

    @Override
    public Collection<LegalTag> get(long[] ids){
        Function<long[],Collection<LegalTag>> func = Retry.decorateFunction(retry, super::get);
        func = CircuitBreaker.decorateFunction(circuitBreaker, func);
        return func.apply(ids);
    }
    @Override
    public Boolean delete(LegalTag legalTag) {
        Function<LegalTag,Boolean> func = Retry.decorateFunction(retry, super::delete);
        func = CircuitBreaker.decorateFunction(circuitBreaker, func);
        return func.apply(legalTag);
    }
    @Override
    public Collection<LegalTag> list(ListLegalTagArgs args){
        Function<ListLegalTagArgs, Collection<LegalTag>> func = Retry.decorateFunction(retry, super::list);
        func = CircuitBreaker.decorateFunction(circuitBreaker, func);
        return func.apply(args);
    }
    @Override
    public LegalTag update(LegalTag newLegalTag){
        Function<LegalTag,LegalTag> func = Retry.decorateFunction(retry, super::update);
        func = CircuitBreaker.decorateFunction(circuitBreaker, func);
        return func.apply(newLegalTag);
    }

    private static Retry createDatastoreRetryConfig(int initialWaitTime){
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .intervalFunction(IntervalFunction.ofExponentialRandomBackoff(initialWaitTime))
                .retryOnException(shouldRetry())
                .build();

        return Retry.of(CONTRACT_DATASTORE, config);
    }

    private static CircuitBreaker createCircuitBreaker(){
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMillis(10000))
                .recordFailure(shouldRecordFailure())
                .ringBufferSizeInHalfOpenState(10)
                .ringBufferSizeInClosedState(30)
                .build();

        return CircuitBreaker.of(CONTRACT_DATASTORE, circuitBreakerConfig);
    }

    private static Predicate<Throwable> shouldRecordFailure(){
        return p ->
        {
            if(p instanceof PersistenceException) {
                int code = ((PersistenceException) p).getCode();
                return code != Code.INVALID_ARGUMENT_VALUE &&
                        code != Code.FAILED_PRECONDITION_VALUE &&
                        code != Code.ALREADY_EXISTS_VALUE &&
                        code != Code.DATA_LOSS_VALUE &&
                        code != Code.NOT_FOUND_VALUE;
            }
            return false;
        };
    }

    private static Predicate<Throwable> shouldRetry(){
        return p ->
        {
            if(p instanceof PersistenceException) {
                int code = ((PersistenceException) p).getCode();
                return code == Code.ABORTED_VALUE ||
                        code == Code.DEADLINE_EXCEEDED_VALUE ||
                        code == Code.UNAVAILABLE_VALUE;
            }
            return  false;
        };
    }
}
