# DOM Refactoring Plan for Neko-HTMLUnit 3.x Migration

## Executive Summary

The migration to neko-htmlunit 3.x requires refactoring httpunit's custom DOM layer (51 classes in `com.meterware.httpunit.dom`). The core issue is that neko 3.x no longer supports custom document implementations, creating incompatibility with httpunit's DOM classes.

## Current Architecture

### Httpunit's Custom DOM Hierarchy
```
NodeImpl (base) → ElementImpl → HTMLElementImpl → Specific HTML elements
                               → HTMLControl → Form controls (input, select, button, etc.)
```

### Key Classes (51 total):
- **Base DOM**: NodeImpl, DocumentImpl, ElementImpl, HTMLElementImpl
- **HTML Elements**: 30+ specific HTML element implementations
- **Form Controls**: HTMLControl (base for form elements)
- **Containers**: HTMLContainerElement, HTMLDocumentImpl

## Problem Analysis

### Why It Fails with Neko 3.x

**Neko 2.x**: Allowed custom document implementations
- Httpunit could pass `com.meterware.httpunit.dom.HTMLDocumentImpl` to parser
- Parser created httpunit's custom DOM elements

**Neko 3.x**: Requires document class extending `org.htmlunit.cyberneko.xerces.dom.DocumentImpl`
- Option 1: Use `org.htmlunit.cyberneko.xerces.dom.DocumentImpl` → Creates plain DOM (not HTML-aware)
- Option 2: Use `org.htmlunit.cyberneko.html.dom.HTMLDocumentImpl` → Creates neko's HTML elements (incompatible with httpunit)

### Current Test Failures: 393 errors / 827 tests

**Pattern 1**: Plain DOM elements lack HTML interfaces
```
ClassCastException: org.htmlunit.cyberneko.xerces.dom.ElementNSImpl 
  cannot be cast to org.w3c.dom.html.HTMLFormElement
```

**Pattern 2**: Neko's HTML elements incompatible with httpunit's classes
```
ClassCastException: org.htmlunit.cyberneko.html.dom.HTMLInputElementImpl
  cannot be cast to com.meterware.httpunit.dom.HTMLControl
```

## Refactoring Strategy

### Approach: Adapter Pattern + Interface Abstraction

Instead of custom DOM classes, create adapters that wrap neko's HTML elements and provide httpunit's API.

### Phase 1: Create Adapter Layer (Week 1-2)

**Step 1.1**: Define adapter interfaces
- `HTMLControlAdapter` - wraps HTML form controls
- `HTMLElementAdapter` - wraps generic HTML elements  
- `HTMLDocumentAdapter` - wraps HTML documents

**Step 1.2**: Implement adapters for neko 3.x elements
- Wrap `org.htmlunit.cyberneko.html.dom.*` elements
- Provide httpunit's expected API methods
- Delegate to underlying neko element

**Step 1.3**: Update ParsedHTML to use adapters
- Replace casts to `HTMLControl` with adapter creation
- Replace casts to HTML interfaces with adapter retrieval

### Phase 2: Update Form Control Classes (Week 3)

**Step 2.1**: Modify FormControl class
- Change from storing `HTMLControl` to storing adapter
- Update all methods to work through adapter

**Step 2.2**: Update Button, WebForm, and related classes
- Replace `HTMLControl` references with adapters
- Maintain backward compatibility where possible

### Phase 3: Update Element Registry (Week 4)

**Step 3.1**: Modify ElementRegistry
- Update registration to work with adapters
- Ensure element lookups return adapted elements

**Step 3.2**: Update HTMLPage
- Modify document handling to use adapters
- Update element traversal logic

### Phase 4: Testing & Validation (Week 5-6)

**Step 4.1**: Incremental testing
- Test each phase independently
- Fix issues before moving to next phase

**Step 4.2**: Full test suite validation
- Target: 95%+ test pass rate (785+ of 827 tests)
- Document any remaining compatibility issues

## Detailed Changes Required

### Files to Modify (Priority Order)

**High Priority (Phase 1)**:
1. `ParsedHTML.java` - 15+ HTMLControl casts
2. `FormControl.java` - Core form control handling
3. `HTMLParserFactory.java` - Parser configuration
4. `NekoDOMParser.java` - Document creation

**Medium Priority (Phase 2)**:
5. `Button.java` - Button handling
6. `WebForm.java` - Form processing
7. `HTMLPage.java` - Page structure
8. `WebResponse.java` - Response handling

**Low Priority (Phase 3-4)**:
9. DOM package classes - May become obsolete with adapter pattern
10. Test classes - Update assertions as needed

### Code Examples

**Before (Current - Fails)**:
```java
HTMLControl control = (HTMLControl) element;  // ClassCastException!
HTMLFormElement form = control.getForm();
```

**After (With Adapters)**:
```java
HTMLControlAdapter control = HTMLControlAdapter.wrap(element);
HTMLFormElement form = control.getForm();
```

## Alternative Approaches Considered

### Alternative 1: Fork Neko 2.x
- **Pros**: No refactoring needed
- **Cons**: Unsustainable, no security updates, defeats migration purpose

### Alternative 2: Switch to Different Parser
- **Pros**: Might have better compatibility
- **Cons**: Different API, still requires refactoring

### Alternative 3: Complete DOM Rewrite
- **Pros**: Clean modern architecture
- **Cons**: 6+ months effort, high risk

## Recommended Approach

**Adapter Pattern** (outlined above):
- **Effort**: 4-6 weeks
- **Risk**: Medium - incremental, testable
- **Benefit**: Maintains httpunit API, achieves neko 3.x compatibility
- **Sustainability**: Future-proof architecture

## Success Criteria

1. ✅ Code compiles with neko-htmlunit 3.11.0+
2. ✅ 95%+ test pass rate (785+/827 tests)
3. ✅ No breaking changes to public API
4. ✅ All form controls functional
5. ✅ Table parsing works correctly
6. ✅ JavaScript event handling intact

## Risk Mitigation

- **Incremental approach**: Test each phase independently
- **Feature flags**: Allow toggling between implementations
- **Comprehensive testing**: Run full suite after each change
- **Backward compatibility**: Maintain existing public APIs

## Timeline Estimate

- **Phase 1**: 2 weeks (Adapter layer)
- **Phase 2**: 1 week (Form controls)
- **Phase 3**: 1 week (Element registry)
- **Phase 4**: 2 weeks (Testing/fixes)
- **Total**: 6 weeks with buffer

## Next Steps

1. Get approval for adapter pattern approach
2. Create feature branch for refactoring
3. Implement Phase 1 (adapter interfaces)
4. Validate with subset of tests
5. Continue through phases with regular progress reports
