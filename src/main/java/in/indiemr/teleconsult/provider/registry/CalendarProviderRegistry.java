package in.indiemr.teleconsult.provider.registry;

import in.indiemr.teleconsult.provider.CalendarProviderAdapter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CalendarProviderRegistry {
    private final Map<String, CalendarProviderAdapter> providers;

    public CalendarProviderRegistry(List<CalendarProviderAdapter> implementations) {
        this.providers = implementations.stream()
            .collect(
                Collectors.toMap(
                    CalendarProviderAdapter::getProviderCode,
                    Function.identity(),
                    (a, b) -> { throw new IllegalStateException("Duplicate calendar provider: " + a.getProviderCode()); }
                )
            );
    }

    public CalendarProviderAdapter require(String providerCode) {
        CalendarProviderAdapter adapter = providers.get(providerCode);
        if (adapter == null) {
            throw new IllegalArgumentException("Unsupported calendar provider: " + providerCode);
        }
        return adapter;
    }
}