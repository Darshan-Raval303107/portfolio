import React, { useState } from 'react'
import ReactDOM from 'react-dom/client'
import './index.css'
const App = () => {
  const [tab, setTab] = useState('Profile')
  return (
    <div>
      <nav style={{ display: 'flex', gap: '20px' }}>
        {['Profile', 'About Me', 'Contact Me', 'Projects'].map(x => <a key={x} href="#" onClick={() => setTab(x)} style={{ textDecoration: tab === x ? 'underline' : 'none' }}>{x}</a>)}
      </nav>
      {tab === 'Profile' && <section><h2>Profile</h2><p>Name: Raval Darshan<br/>Email: ravaldarshan079@gmail.com</p></section>}
      {tab === 'About Me' && <section><h2>About Me</h2><p>I am a full stack developer.</p></section>}
      {tab === 'Contact Me' && <section><h2>Contact Me</h2><p>Mobile: (+91) 9510704284</p></section>}
      {tab === 'Projects' && <section><h2>Projects</h2><p>Restaurant Management System: <a href="https://vinod-dinig-hall.vercel.app/window" target="_blank" rel="noreferrer">Vinod Dining Hall</a></p></section>}
    </div>
  )
}
ReactDOM.createRoot(document.getElementById('root')).render(<App />)
