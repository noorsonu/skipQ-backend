package in.skipQ.platformconfig.repository;

import in.skipQ.platformconfig.model.ConfigScope;
import in.skipQ.platformconfig.model.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, UUID> {

    Optional<FeatureFlag> findByFlagKeyAndScopeAndScopeId(String flagKey, ConfigScope scope, UUID scopeId);

    Optional<FeatureFlag> findByFlagKeyAndScope(String flagKey, ConfigScope scope);

    List<FeatureFlag> findAllByOrderByFlagKeyAsc();
}

