# Contributing

We welcome contributions to the MochaAPI Client library! Here's how you can help make it better.

## How to Contribute

### 1. Fork the Repository

1. Go to [MochaAPI Client on GitHub](https://github.com/guptavishal-xm1/MochaJSON)
2. Click the "Fork" button in the top-right corner
3. Clone your fork locally:

```bash
git clone https://github.com/guptavishal-xm1/MochaJSON.git
cd MochaJSON
```

### 2. Create a Feature Branch

```bash
git checkout -b feature/amazing-feature
```

### 3. Make Your Changes

- Write clean, readable code
- Follow existing code style and conventions
- Add Javadoc/KDoc comments for public APIs
- Include unit tests for new features
- Update documentation if needed

### 4. Test Your Changes

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run example
./gradlew run
```

### 5. Commit Your Changes

```bash
git add .
git commit -m "Add amazing feature"
```

Use clear, descriptive commit messages:
- `Add support for custom headers`
- `Fix JSON parsing for nested objects`
- `Update documentation for async methods`

### 6. Push to Your Branch

```bash
git push origin feature/amazing-feature
```

### 7. Open a Pull Request

1. Go to your fork on GitHub
2. Click "New Pull Request"
3. Select your feature branch
4. Fill out the pull request template
5. Submit the pull request

## Development Setup

### Prerequisites

- **Java 21+** - Required for building and running
- **Gradle 8.14+** - Build system
- **Git** - Version control

### Local Development

```bash
# Clone the repository
git clone https://github.com/guptavishal-xm1/MochaJSON.git
cd MochaJSON

# Build the project
./gradlew build

# Run tests
./gradlew test

# Run example
./gradlew run

# Clean build
./gradlew clean build
```

### IDE Setup

#### IntelliJ IDEA

1. Open the project folder
2. Import Gradle project when prompted
3. Configure Java 21+ as project SDK
4. Enable annotation processing if needed

#### Eclipse

1. Import as Gradle project
2. Configure Java 21+ as project JDK
3. Refresh Gradle dependencies

#### VS Code

1. Install Java Extension Pack
2. Open the project folder
3. Configure Java 21+ in settings

## Code Style Guidelines

### Java Code Style

- **Indentation**: 4 spaces (no tabs)
- **Line Length**: Maximum 120 characters
- **Naming**: Follow Java conventions
  - Classes: `PascalCase`
  - Methods/Variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
- **Comments**: Javadoc for public APIs

```java
/**
 * Creates a new API request with the specified URL and HTTP method.
 * 
 * @param url the target URL
 * @param method the HTTP method (GET, POST, PUT, DELETE, PATCH)
 */
public ApiRequest(String url, String method) {
    // Implementation
}
```

### Kotlin Code Style

- **Indentation**: 4 spaces (no tabs)
- **Line Length**: Maximum 120 characters
- **Naming**: Follow Kotlin conventions
  - Classes: `PascalCase`
  - Functions/Variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
- **Comments**: KDoc for public APIs

```kotlin
/**
 * Creates a new KotlinxJsonMapper with default configuration.
 */
class KotlinxJsonMapper : JsonMapper {
    // Implementation
}
```

### File Organization

```
src/main/java/io/mochaapi/client/
├── Api.java                    # Main entry point
├── ApiRequest.java             # Request builder
├── ApiResponse.java            # Response container
├── JsonMapper.java             # JSON interface
├── JacksonJsonMapper.java      # Jackson implementation
├── KotlinxJsonMapper.kt        # Kotlinx implementation
├── HttpClientEngine.java       # HTTP interface
├── internal/
│   ├── DefaultHttpClientEngine.java
│   └── Utils.java
├── exception/
│   ├── ApiException.java
│   └── JsonException.java
└── example/
    └── ExampleUsage.java
```

## Testing Guidelines

### Unit Tests

- Write tests for all public methods
- Test both success and error scenarios
- Use descriptive test names
- Follow AAA pattern (Arrange, Act, Assert)

```java
@Test
@DisplayName("GET request with Map response")
public void testGetRequestWithMapResponse() {
    // Arrange
    String url = "https://jsonplaceholder.typicode.com/posts/1";
    
    // Act
    ApiResponse response = Api.get(url).execute();
    
    // Assert
    assertNotNull(response);
    assertTrue(response.isSuccess());
    assertEquals(200, response.code());
    
    Map<String, Object> post = response.toMap();
    assertNotNull(post);
    assertTrue(post.containsKey("id"));
    assertTrue(post.containsKey("title"));
}
```

### Integration Tests

- Test with real APIs when possible
- Use JSONPlaceholder for testing
- Test error scenarios (404, 500, etc.)

### Test Coverage

- Aim for high test coverage (>90%)
- Test edge cases and error conditions
- Include performance tests for critical paths

## Documentation Guidelines

### README Updates

- Update README.md for new features
- Include code examples
- Update installation instructions if needed
- Add new dependencies to the list

### API Documentation

- Add Javadoc/KDoc for new public methods
- Include parameter descriptions
- Add usage examples
- Document exceptions that can be thrown

### Example Code

- Update ExampleUsage.java for new features
- Add new examples to documentation
- Ensure all examples are compilable
- Test examples before committing

## Reporting Issues

### Before Reporting

1. **Search existing issues** - Check if the issue already exists
2. **Check documentation** - Make sure it's not a usage issue
3. **Test with latest version** - Ensure you're using the latest release

### Issue Template

When creating an issue, include:

```markdown
## Bug Report / Feature Request

### Description
Brief description of the issue or feature request.

### Steps to Reproduce (for bugs)
1. 
2. 
3. 

### Expected Behavior
What you expected to happen.

### Actual Behavior
What actually happened.

### Environment
- Java Version: 
- Kotlin Version: 
- MochaAPI Client Version: 
- OS: 

### Additional Context
Any other relevant information, screenshots, or code examples.
```

### Bug Reports

- **Clear Description**: Explain what went wrong
- **Steps to Reproduce**: Provide exact steps
- **Expected vs Actual**: What should happen vs what did happen
- **Environment**: Java version, OS, library version
- **Code Example**: Minimal code that reproduces the issue

### Feature Requests

- **Use Case**: Explain why this feature is needed
- **Proposed Solution**: How you think it should work
- **Alternatives**: Other ways to solve the problem
- **Additional Context**: Any other relevant information

## Pull Request Guidelines

### PR Template

```markdown
## Description
Brief description of changes.

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] All tests pass
- [ ] Manual testing completed

## Documentation
- [ ] README updated
- [ ] API documentation updated
- [ ] Examples updated
- [ ] Changelog updated

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] No breaking changes (or documented)
- [ ] Ready for review
```

### Review Process

1. **Self Review**: Review your own changes before submitting
2. **CI Checks**: Ensure all CI checks pass
3. **Code Review**: Address reviewer feedback
4. **Testing**: Verify changes work as expected
5. **Merge**: Maintainer merges the PR

## Release Process

### Version Numbering

We follow [Semantic Versioning](https://semver.org/):

- **MAJOR**: Breaking changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes (backward compatible)

### Release Checklist

- [ ] All tests pass
- [ ] Documentation updated
- [ ] Examples tested
- [ ] Version number updated
- [ ] Changelog updated
- [ ] Release notes prepared
- [ ] Tag created
- [ ] Maven Central published

## Community Guidelines

### Code of Conduct

- **Be Respectful**: Treat everyone with respect
- **Be Constructive**: Provide helpful feedback
- **Be Patient**: Remember that contributors are volunteers
- **Be Professional**: Keep discussions focused and productive

### Getting Help

- **GitHub Issues**: For bugs and feature requests
- **GitHub Discussions**: For questions and general discussion
- **Pull Requests**: For code contributions

## Recognition

Contributors will be recognized in:

- **README.md**: Listed as contributors
- **Release Notes**: Mentioned in release announcements
- **GitHub**: Shown in contributor statistics

## License

By contributing to MochaAPI Client, you agree that your contributions will be licensed under the MIT License.

## Questions?

If you have questions about contributing:

1. Check existing [GitHub Issues](https://github.com/guptavishal-xm1/MochaJSON/issues)
2. Start a [GitHub Discussion](https://github.com/guptavishal-xm1/MochaJSON/discussions)
3. Review the [API Documentation](/MochaJSON/api/overview)

Thank you for contributing to MochaAPI Client! 🚀
