import React, { useState } from 'react';
import Navbar from './components/Navbar.jsx';
import Analyzer from './pages/Analyzer.jsx';
import GameHistory from './pages/GameHistory.jsx';

export default function App() {
  const [route, setRoute] = useState('analyzer');

  return (
    <div className="min-h-screen bg-chess-dark">
      <Navbar route={route} onNavigate={setRoute} />
      <main className="mx-auto max-w-6xl px-4 py-6">
        {route === 'analyzer' ? <Analyzer /> : <GameHistory />}
      </main>
    </div>
  );
}

