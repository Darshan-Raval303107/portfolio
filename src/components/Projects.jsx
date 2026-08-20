import React from 'react';
import { motion } from 'framer-motion';
import { ExternalLink, Github, Utensils } from 'lucide-react';
import './Projects.css';

const Projects = () => {
  const projects = [
    {
      id: 1,
      title: 'Vinod Dining Hall',
      category: 'Full Stack Web App',
      description: 'An interactive restaurant management system enabling smooth order tracking, real-time table occupancy dashboards, menu updating, and invoice generation workflows.',
      imageClass: 'proj-vinod',
      tags: ['React', 'Python', 'Flask', 'NeonDB', 'CSS Modules'],
      liveLink: 'https://vinod-dinig-hall.vercel.app/window',
      icon: Utensils,
    },
  ];


  const containerVariants = {
    hidden: { opacity: 0 },
    show: {
      opacity: 1,
      transition: {
        staggerChildren: 0.15,
      },
    },
  };

  const itemVariants = {
    hidden: { opacity: 0, y: 30 },
    show: { opacity: 1, y: 0, transition: { duration: 0.5, ease: 'easeOut' } },
  };

  return (
    <motion.section
      className="projects-section"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      transition={{ duration: 0.5 }}
    >
      <div className="section-header text-center">
        <h2 className="section-title">My <span className="gradient-text">Projects</span></h2>
      </div>

      <motion.div 
        className="projects-grid"
        variants={containerVariants}
        initial="hidden"
        animate="show"
      >
        {projects.map((project) => {
          const IconComponent = project.icon;
          return (
            <motion.div 
              key={project.id} 
              className="project-card glass-card"
              variants={itemVariants}
            >
              {/* Card visual header */}
              <div className="project-banner">
                <div className="banner-overlay" />
                <div className="project-icon-box">
                  <IconComponent size={28} />
                </div>
                <span className="project-category">{project.category}</span>
              </div>

              {/* Card content */}
              <div className="project-details">
                <h3 className="project-title">{project.title}</h3>
                <p className="project-description">{project.description}</p>
                
                {/* Tech tags */}
                <div className="project-tags">
                  {project.tags.map((tag, idx) => (
                    <span key={idx} className="project-tag-badge">{tag}</span>
                  ))}
                </div>

                {/* Actions */}
                <div className="project-links">
                  {project.githubLink && (
                    <a href={project.githubLink} className="proj-link-btn outline-btn" aria-label="GitHub Repository">
                      <Github size={16} /> Code
                    </a>
                  )}
                  {project.liveLink && (
                    <a href={project.liveLink} target="_blank" rel="noreferrer" className="proj-link-btn solid-btn" aria-label="Live Demo">
                      <ExternalLink size={16} /> Live Demo
                    </a>
                  )}
                </div>
              </div>
            </motion.div>
          );
        })}
      </motion.div>
    </motion.section>
  );
};

export default Projects;
