

package in.indiemr.teleconsult.provider.registry;

import in.indiemr.teleconsult.provider.MeetingProviderAdapter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class MeetingProviderRegistry {
    private final Map<String, MeetingProviderAdapter> providers;

    public MeetingProviderRegistry(List<MeetingProviderAdapter> implementations) {
        this.providers = implementations.stream()
            .collect(
                Collectors.toMap(
                    MeetingProviderAdapter::getProviderCode, 
                    Function.identity(),
                    (a, b) -> { throw new IllegalStateException("Duplicate OAuth provider: " + a.getProviderCode()); }
                )
            );
    }

    public MeetingProviderAdapter require(String providerCode) {
        MeetingProviderAdapter adapter = providers.get(providerCode);
        if (adapter == null) {
            throw new IllegalArgumentException("Unsupported OAuth provider: " + providerCode);
        }
        return adapter;
    }
}
