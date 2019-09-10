package info.m2sj.springrouterdatasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public class MyRoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        Object dbKey = RequestContextHolder.getRequestAttributes().getAttribute("db_key", RequestAttributes.SCOPE_SESSION);
        return "current:" + dbKey;
    }
}
