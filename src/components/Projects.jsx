import React, { useState, useEffect } from 'react';
import { ExternalLink, Github, Search, X } from 'lucide-react';
import LoadingSpinner from './LoadingSpinner';
import ErrorMessage from './ErrorMessage';
import './Projects.css';

// GitHub username to fetch repositories for
const GITHUB_USERNAME = 'Darshan-Raval303107';

const Projects = () => {
  // State variables for repositories, search input, loading status, and error
  const [repos, setRepos] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Function to fetch repositories from GitHub REST API
  const fetchRepositories = async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await fetch(
        `https://api.github.com/users/${GITHUB_USERNAME}/repos?sort=updated`
      );

      if (!response.ok) {
        throw new Error(`Failed to fetch repositories (Status: ${response.status})`);
      }

      const data = await response.json();
      setRepos(data);
    } catch (err) {
      setError(err.message || 'Something went wrong while fetching data');
    } finally {
      setLoading(false);
    }
  };

  // Fetch repositories once when component mounts
  useEffect(() => {
    fetchRepositories();
  }, []);

  // Filter repositories based on search term (matches name, language, or description)
  const filteredRepos = repos.filter((repo) => {
    const query = searchTerm.toLowerCase();
    const nameMatches = repo.name.toLowerCase().includes(query);
    const langMatches = repo.language && repo.language.toLowerCase().includes(query);
    const descMatches = repo.description && repo.description.toLowerCase().includes(query);
    return nameMatches || langMatches || descMatches;
  });

  return (
    <section className="projects-section">
      {/* Header */}
      <div className="section-header text-center">
        <h2 className="section-title">
          My <span className="gradient-text">Projects</span>
        </h2>
      </div>

      {/* Search Bar */}
      <div className="search-container">
        <div className="search-bar">
          <Search className="search-icon" size={18} />
          <input
            type="text"
            className="search-input"
            placeholder="Search projects by name or language (e.g. JavaScript, LeetCode)..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          {searchTerm && (
            <button
              className="clear-search-btn"
              onClick={() => setSearchTerm('')}
              title="Clear search"
            >
              <X size={16} />
            </button>
          )}
        </div>
        {!loading && !error && (
          <p className="search-count">
            Showing {filteredRepos.length} of {repos.length} repositories
          </p>
        )}
      </div>

      {/* 1. Loading Spinner while request is in progress */}
      {loading && <LoadingSpinner />}

      {/* 2. Error Message if API call fails */}
      {!loading && error && (
        <ErrorMessage message={error} onRetry={fetchRepositories} />
      )}

      {/* 3. Repositories Grid */}
      {!loading && !error && (
        <>
          {filteredRepos.length > 0 ? (
            <div className="projects-grid">
              {filteredRepos.map((repo) => (
                <div key={repo.id} className="project-card glass-card">
                  <div className="project-details">
                    <div className="repo-header">
                      <h3 className="project-title">{repo.name}</h3>
                      {repo.language && (
                        <span className="project-tag-badge">{repo.language}</span>
                      )}
                    </div>

                    <p className="project-description">
                      {repo.description || 'No description available for this repository.'}
                    </p>

                    {/* Repository Links */}
                    <div className="project-links">
                      <a
                        href={repo.html_url}
                        target="_blank"
                        rel="noreferrer"
                        className="proj-link-btn solid-btn"
                      >
                        <Github size={16} /> View Repo
                      </a>
                      {repo.homepage && (
                        <a
                          href={repo.homepage}
                          target="_blank"
                          rel="noreferrer"
                          className="proj-link-btn outline-btn"
                        >
                          <ExternalLink size={16} /> Live Demo
                        </a>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="no-results glass-card">
              <p className="no-results-text">
                No projects found matching "<strong>{searchTerm}</strong>"
              </p>
              <button className="clear-btn" onClick={() => setSearchTerm('')}>
                Clear Search
              </button>
            </div>
          )}
        </>
      )}
    </section>
  );
};

export default Projects;
