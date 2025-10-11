# Architectural Decision: Port Interface Implementation (Deferred)

## Context

During Gemini Code Assist review of PR #7 (Site Registration API - CRAW-70), a High priority architectural concern was raised:

> **Gemini Feedback**: Controllers currently depend directly on UseCase implementations, violating Hexagonal Architecture's Dependency Inversion Principle. Inbound ports (interfaces) should be created for all UseCases, with UseCases implementing these interfaces and controllers depending on the interfaces rather than implementations.

**Current Architecture:**
```java
// Controller depends directly on implementation
@RestController
public class CrawlSiteController {
    private final RegisterSiteUseCase registerSiteUseCase;  // Concrete dependency
}
```

**Recommended Architecture:**
```java
// Controller depends on interface (port)
@RestController
public class CrawlSiteController {
    private final RegisterSiteInPort registerSiteInPort;  // Interface dependency
}

// UseCase implements interface
@Service
public class RegisterSiteUseCase implements RegisterSiteInPort {
    // Implementation
}
```

## Decision

**DEFER** implementation of inbound port interfaces to a separate Epic for the following reasons:

### Scope Considerations

1. **Affected Components** (4 UseCases × ~3 files each = ~12 files):
   - `RegisterSiteUseCase` → `RegisterSiteInPort` interface
   - `GetSiteUseCase` → `GetSiteInPort` interface
   - `UpdateSiteUseCase` → `UpdateSiteInPort` interface
   - `DeleteSiteUseCase` → `DeleteSiteInPort` interface
   - All 4 controller dependencies must be updated
   - Architecture tests must be updated to verify new pattern

2. **Estimated Effort**: 2-3 hours
   - Create 4 new port interfaces in application layer
   - Refactor 4 UseCases to implement interfaces
   - Update controller dependencies and injection
   - Update Spring configuration
   - Modify ArchUnit tests to enforce new pattern
   - Verify all integration tests still pass

3. **PR Focus**:
   - Current PR (CRAW-70) is focused on **feature implementation** (Site Registration API)
   - Architectural refactoring should be separate from feature work
   - Mixing concerns makes PR review more difficult and increases merge conflict risk

### Technical Debt Acknowledgment

This is legitimate architectural debt that should be addressed, but not as part of CRAW-70. The current implementation:
- ✅ **Works correctly** - All functionality is properly implemented
- ✅ **Has clear boundaries** - UseCases are in application layer, controllers in adapter layer
- ✅ **Is testable** - Integration tests verify behavior end-to-end
- ⚠️ **Lacks interface abstraction** - Direct dependency on implementations

### Recommended Follow-up

**Create New Epic**: "Implement Hexagonal Architecture Inbound Ports"

**User Stories**:
1. As a developer, I want UseCase interfaces (inbound ports) so that controllers depend on abstractions, not implementations
2. As a developer, I want architecture tests to enforce port-based dependency rules
3. As a developer, I want consistent port naming conventions across the codebase

**Acceptance Criteria**:
- [ ] Create `RegisterSiteInPort`, `GetSiteInPort`, `UpdateSiteInPort`, `DeleteSiteInPort` interfaces
- [ ] Refactor all 4 UseCases to implement their respective port interfaces
- [ ] Update all controller dependencies to use port interfaces
- [ ] Add ArchUnit test: "Controllers should depend only on interfaces ending with 'InPort'"
- [ ] Update architecture documentation with port pattern guidelines
- [ ] Verify all existing integration tests pass without modification

**Benefits of Separate Epic**:
- Clear scope boundary for architectural refactoring
- Can be prioritized independently from feature work
- Easier to review and validate architectural compliance
- Reduces risk of merge conflicts with feature branches
- Allows for comprehensive testing of architectural changes

## References

- **PR #7**: Site Registration API (CRAW-70)
- **Gemini Review**: 2025-10-11 09:04:27 KST (Third review cycle)
- **Architecture Pattern**: Hexagonal Architecture (Ports & Adapters)
- **Principle**: Dependency Inversion Principle (SOLID)

## Related Documentation

- `architecture/hexagonal-architecture.md` - Overall architecture guidelines (TODO)
- `architecture/naming-conventions.md` - Port interface naming patterns (TODO)

---

**Created**: 2025-10-11
**Decision Owner**: @ryu-qqq
**Status**: DEFERRED
**Target Epic**: TBD
