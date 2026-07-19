import React, { useState } from 'react';
import { AnimatePresence } from 'framer-motion';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import Profile from './components/Profile';
import About from './components/About';
import Projects from './components/Projects';
import Contact from './components/Contact';

const App = () => {
  const [activeTab, setActiveTab] = useState('Profile');

  const renderContent = () => {
    switch (activeTab) {
      case 'Profile':
        return <Profile key="profile" setActiveTab={setActiveTab} />;
      case 'About Me':
        return <About key="about" />;
      case 'Projects':
        return <Projects key="projects" />;
      case 'Contact Me':
        return <Contact key="contact" />;
      default:
        return <Profile key="profile" setActiveTab={setActiveTab} />;
    }
  };

  return (
    <div className="app-container">
      {/* Premium Navbar */}
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} />

      {/* Main Content Area with entrance transitions */}
      <main className="main-content">
        <AnimatePresence mode="wait">
          {renderContent()}
        </AnimatePresence>
      </main>

      {/* Premium Footer */}
      <Footer setActiveTab={setActiveTab} />
    </div>
  );
};

export default App;
