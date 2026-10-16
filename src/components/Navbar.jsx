import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { User, Info, FolderGit2, Mail, CheckSquare, Menu, X } from 'lucide-react';
import './Navbar.css';

const Navbar = ({ activeTab, setActiveTab }) => {
  const [isOpen, setIsOpen] = useState(false);

  const navItems = [
    { id: 'Profile', label: 'Profile', icon: User },
    { id: 'About Me', label: 'About Me', icon: Info },
    { id: 'Projects', label: 'Projects', icon: FolderGit2 },
    { id: 'Todo App', label: 'Todo App', icon: CheckSquare },
    { id: 'Contact Me', label: 'Contact Me', icon: Mail },
  ];

  return (
    <nav className="navbar-container">
      <div className="navbar-content">
        {/* Logo */}
        <motion.div 
          className="navbar-logo"
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.5 }}
        >
          <span className="logo-symbol">&lt;/&gt;</span>
          <span className="logo-text">Darshan<span className="logo-text-gradient">.Dev</span></span>
        </motion.div>

        {/* Desktop Menu */}
        <div className="navbar-desktop-menu">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setActiveTab(item.id)}
                className={`nav-item-btn ${isActive ? 'active' : ''}`}
              >
                <Icon size={16} className="nav-item-icon" />
                <span>{item.label}</span>
                {isActive && (
                  <motion.div
                    layoutId="activeTabPill"
                    className="active-tab-pill"
                    transition={{ type: 'spring', stiffness: 380, damping: 30 }}
                  />
                )}
              </button>
            );
          })}
        </div>

        {/* Mobile Toggle Button */}
        <div className="navbar-mobile-toggle">
          <button onClick={() => setIsOpen(!isOpen)} aria-label="Toggle menu" className="toggle-btn">
            {isOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>
      </div>

      {/* Mobile Drawer Menu */}
      <AnimatePresence>
        {isOpen && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }}
            transition={{ duration: 0.3, ease: 'easeInOut' }}
            className="navbar-mobile-menu"
          >
            <div className="mobile-menu-items">
              {navItems.map((item) => {
                const Icon = item.icon;
                const isActive = activeTab === item.id;
                return (
                  <button
                    key={item.id}
                    onClick={() => {
                      setActiveTab(item.id);
                      setIsOpen(false);
                    }}
                    className={`mobile-nav-item-btn ${isActive ? 'active' : ''}`}
                  >
                    <Icon size={18} className="nav-item-icon" />
                    <span>{item.label}</span>
                    {isActive && <div className="active-dot" />}
                  </button>
                );
              })}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </nav>
  );
};

export default Navbar;
