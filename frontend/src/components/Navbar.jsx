import React from 'react';

export default function Navbar({ route, onNavigate }) {
  return (
    <header className="border-b border-white/10 bg-chess-dark/70 backdrop-blur">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
        <div className="flex items-center gap-3">
          <div className="h-10 w-10 rounded bg-chess-wood/60 flex items-center justify-center font-black tracking-wide">
            CA
          </div>
          <div>
            <div className="text-sm text-white/70">Chess Game Analyzer</div>
            <div className="text-lg font-bold">Analyzer</div>
          </div>
        </div>

        <nav className="flex items-center gap-2">
          <button
            className={`px-3 py-2 rounded border text-sm ${
              route === 'analyzer'
                ? 'border-chess-accent bg-green-600/20 text-white'
                : 'border-white/10 bg-white/0 text-white/70 hover:text-white'
            }`}
            onClick={() => onNavigate('analyzer')}
          >
            Analyze
          </button>
          <button
            className={`px-3 py-2 rounded border text-sm ${
              route === 'history'
                ? 'border-chess-accent bg-green-600/20 text-white'
                : 'border-white/10 bg-white/0 text-white/70 hover:text-white'
            }`}
            onClick={() => onNavigate('history')}
          >
            History
          </button>
        </nav>
      </div>
    </header>
  );
}

