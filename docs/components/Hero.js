import React from 'react';

const Hero = () => {
  return (
    <div className="hero-gradient text-center py-16 px-4">
      <div className="max-w-4xl mx-auto">
        {/* Badges */}
        <div className="flex justify-center space-x-4 mb-8 flex-wrap gap-2">
          <span className="badge badge-success">
            <span className="hero-badge-icon">✓</span>
            Production Ready
          </span>
          <span className="badge badge-success">
            <span className="hero-badge-icon">📄</span>
            MIT Licensed
          </span>
          <span className="badge badge-success">
            <span className="hero-badge-icon">☕</span>
            Java & Kotlin
          </span>
          <span className="badge badge-purple">
            <span className="hero-badge-icon">🚀</span>
            v1.3.0
          </span>
        </div>

        {/* Main Title */}
        <h1 className="text-4xl md:text-6xl font-bold text-white mb-6 hero-title">
          MochaJSON
        </h1>
        
        {/* Subtitle */}
        <p className="text-xl md:text-2xl text-gray-200 mb-8 hero-subtitle">
          Simplified HTTP & JSON Library for Java & Kotlin
        </p>
        
        {/* Description */}
        <p className="text-lg text-gray-300 mb-12 max-w-2xl mx-auto">
          Fast, simple, and chainable API calls with automatic JSON parsing, zero boilerplate, and essential features like simple retry, security controls, and async operations.
        </p>

        {/* CTA Buttons */}
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <a
            href="/MochaJSON/getting-started"
            className="btn-primary text-white no-underline inline-flex items-center justify-center gap-2"
          >
            <span className="hero-button-icon">🚀</span>
            Get Started
          </a>
          
          <a
            href="/MochaJSON/api/overview"
            className="btn-secondary text-white no-underline border-2 border-gray-300 bg-transparent hover:bg-gray-300 hover:text-gray-900 inline-flex items-center justify-center gap-2"
          >
            <span className="hero-button-icon">📚</span>
            View API
          </a>
        </div>

        {/* Quick Example */}
        <div className="mt-16 bg-black bg-opacity-20 rounded-lg p-6 max-w-4xl mx-auto">
          <div className="text-left">
            <h3 className="text-lg font-semibold text-white mb-4">Quick Example</h3>
            <div className="bg-gray-900 rounded-lg p-4 overflow-x-auto">
              <pre className="text-green-400 text-sm">
                <code>{`// One import, one line, automatic JSON parsing
import io.mochaapi.client.*;

// Basic usage - works in both Java and Kotlin
Map<String, Object> user = Api.get("https://api.github.com/users/octocat")
    .execute()
    .toMap();

System.out.println("User: " + user.get("name"));

// Advanced usage with v1.3.0 simplified features
ApiClient client = new ApiClient.Builder()
    .enableRetry()                    // Simple retry with 3 attempts
    .allowLocalhost(true)             // Development-friendly
    .build();

CompletableFuture<ApiResponse> future = client.get("https://api.github.com/user")
    .executeAsync();`}</code>
              </pre>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Hero;
