package in.indiemr.teleconsult.provider.registry;

import in.indiemr.teleconsult.provider.OAuthProviderAdapter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class OAuthProviderRegistry {
    private final Map<String, OAuthProviderAdapter> providers;

    public OAuthProviderRegistry(List<OAuthProviderAdapter> implementations) {
        this.providers = implementations.stream()
            .collect(
                Collectors.toMap(
                    OAuthProviderAdapter::getProviderCode, 
                    Function.identity(),
                    (a, b) -> { throw new IllegalStateException("Duplicate OAuth provider: " + a.getProviderCode()); }
                )
            );
    }

    public OAuthProviderAdapter require(String providerCode) {
        OAuthProviderAdapter adapter = providers.get(providerCode);
        if (adapter == null) {
            throw new IllegalArgumentException("Unsupported OAuth provider: " + providerCode);
        }
        return adapter;
    }
}
