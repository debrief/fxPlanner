# Contributing to fxPlanner

Thank you for your interest in contributing to fxPlanner! This document provides guidelines and information for contributors.

## Code of Conduct

Be respectful, inclusive, and considerate of others. We're all here to build something great together.

## How to Contribute

### Reporting Bugs

1. **Search existing issues** to avoid duplicates
2. **Create a new issue** with:
   - Clear, descriptive title
   - Steps to reproduce
   - Expected vs actual behavior
   - Screenshots if applicable
   - Environment details (OS, Java version, etc.)

### Suggesting Features

1. **Check existing issues/discussions** for similar ideas
2. **Create a new issue** with:
   - Clear use case description
   - Expected behavior
   - Potential implementation approach
   - Why this feature would be valuable

### Submitting Pull Requests

#### Step 1: Fork & Clone

```bash
# Fork the repository on GitHub
# Then clone your fork
git clone https://github.com/YOUR-USERNAME/fxPlanner.git
cd fxPlanner
```

#### Step 2: Create a Branch

```bash
git checkout -b feature/your-feature-name
```

Branch naming conventions:
- `feature/` - New features
- `fix/` - Bug fixes
- `docs/` - Documentation updates
- `refactor/` - Code refactoring
- `test/` - Test additions/updates

#### Step 3: Make Changes

- Follow existing code style
- Write clear commit messages
- Test your changes locally
- Update documentation if needed

#### Step 4: Test

```bash
# Compile and test
mvn clean compile

# Run the application
mvn javafx:run

# Test with JPro (optional)
mvn jpro:run
```

#### Step 5: Commit

```bash
git add .
git commit -m "Brief description of changes"
```

Commit message guidelines:
- Use present tense ("Add feature" not "Added feature")
- First line: concise summary (50 chars or less)
- Blank line, then detailed explanation if needed
- Reference issues: "Fixes #123" or "Relates to #456"

#### Step 6: Push and Create PR

```bash
git push origin feature/your-feature-name
```

Then on GitHub:
1. Click "Compare & pull request"
2. Fill in the PR template
3. Link related issues
4. Wait for the preview to deploy (~5-7 minutes)
5. Test your changes in the browser preview
6. Address any review comments

## Development Guidelines

### Code Style

#### Java

- **Indentation:** 4 spaces (no tabs)
- **Line length:** 120 characters max
- **Naming:**
  - Classes: `PascalCase`
  - Methods/variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
- **Comments:** JavaDoc for public methods/classes

Example:
```java
/**
 * Represents a planning waypoint in the UXV mission.
 */
public class Waypoint {
    private static final int DEFAULT_ALTITUDE = 100;
    
    /**
     * Calculates the distance to another waypoint.
     * @param other the target waypoint
     * @return distance in meters
     */
    public double distanceTo(Waypoint other) {
        // Implementation
    }
}
```

#### JavaFX

- Use FXML for complex layouts when possible
- Separate UI logic from business logic
- Follow JavaFX best practices:
  - Use `Platform.runLater()` for UI updates from background threads
  - Prefer observable properties for data binding
  - Use CSS for styling

### Testing

Currently, the project focuses on manual testing. When adding tests:

```java
// Place tests in src/test/java/
package com.planetmayo.fxplanner;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WaypointTest {
    @Test
    void testDistanceCalculation() {
        // Test implementation
    }
}
```

Run tests:
```bash
mvn test
```

### Documentation

Update documentation when:
- Adding new features
- Changing existing behavior
- Fixing bugs that might confuse users
- Modifying setup/deployment process

Files to update:
- `README.md` - Project overview and quick start
- `docs/ARCHITECTURE.md` - Technical changes
- `docs/TROUBLESHOOTING.md` - New known issues
- JavaDoc comments in code

### JavaFX + JPro Compatibility

When adding JavaFX features:

1. **Test with JPro:**
   ```bash
   mvn jpro:run
   # Open http://localhost:8080
   ```

2. **Check compatibility:**
   - Review [JPro Compatibility Matrix](https://www.jpro.one/docs/current/2.6/COMPATIBILITY)
   - Test in browser preview
   - Document any limitations

3. **Common JPro limitations:**
   - Native file dialogs → Use JPro FileManager API
   - Some 3D features → May not render correctly
   - Hardware acceleration → Limited in browser

4. **Provide fallbacks:**
   ```java
   if (JPro.isJProApplication(stage)) {
       // JPro-specific implementation
   } else {
       // Desktop JavaFX implementation
   }
   ```

## Pull Request Process

### Before Submitting

- [ ] Code compiles without errors
- [ ] Application runs locally
- [ ] No compiler warnings introduced
- [ ] Documentation updated
- [ ] Commit messages are clear

### After Submitting

1. **Automated checks run:**
   - Docker build succeeds
   - Preview deploys to Fly.io
   - Bot posts preview URL

2. **Review process:**
   - Maintainer reviews code
   - Maintainer tests preview
   - Discussion/feedback provided

3. **Addressing feedback:**
   - Make requested changes
   - Push to same branch
   - Preview automatically updates

4. **Approval & merge:**
   - Maintainer approves PR
   - PR merged to main
   - Preview environment cleaned up

## Preview System Usage

### For Contributors

Your PR automatically gets a preview deployment:

1. Open PR → Bot comments "Building..."
2. Wait 5-7 minutes → Bot updates with preview URL
3. Click link → Test your changes in browser
4. Make updates → Push to branch → Preview rebuilds
5. PR merged/closed → Preview destroyed

### Testing the Preview

When your preview is ready:

1. Click the preview URL in the PR comment
2. Wait for the app to load
3. Test your changes thoroughly:
   - UI appearance
   - Button interactions
   - Expected behavior
4. Check browser console for errors (F12 → Console)
5. Test on mobile if relevant

### Preview Limitations

- Single user at a time (no session sharing)
- May have slight rendering differences from desktop
- Network latency affects responsiveness
- Some JavaFX features may not work (check JPro docs)

## Issue Labels

We use these labels to organize issues:

- `bug` - Something isn't working
- `enhancement` - New feature or request
- `documentation` - Documentation improvements
- `good first issue` - Good for newcomers
- `help wanted` - Extra attention needed
- `question` - Further information requested
- `wontfix` - Will not be addressed
- `duplicate` - Duplicate issue

## Release Process

(For maintainers)

1. Version bump in `pom.xml`
2. Update `CHANGELOG.md`
3. Create release branch: `release/v1.x.x`
4. Test thoroughly
5. Merge to main
6. Tag release: `git tag v1.x.x`
7. Push tag: `git push origin v1.x.x`
8. Create GitHub release with notes

## Getting Help

### For Contributors

- 📖 Check `docs/` directory first
- 💬 Use GitHub Discussions for questions
- 🐛 Create an issue for bugs
- 📧 Contact maintainers via GitHub

### For Maintainers

- Setup questions → See `docs/SETUP.md`
- Technical details → See `docs/ARCHITECTURE.md`
- Common issues → See `docs/TROUBLESHOOTING.md`

## Recognition

Contributors are recognized in:
- GitHub contributor graph
- Release notes
- `CONTRIBUTORS.md` (if established)

## License

By contributing, you agree that your contributions will be licensed under the same license as the project (see LICENSE file).

---

Thank you for contributing to fxPlanner! 🎉

Questions? Open an issue or discussion on GitHub.
