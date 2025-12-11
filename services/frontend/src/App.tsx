import React from 'react';
import './App.css';

function App() {
  return (
    <div className="App">
      <header className="App-header">
        <h1>eHealth - Système d'Information Hospitalier Intégré</h1>
        <p>Architecture Hub-and-Spoke pour la gestion hospitalière</p>
        <nav>
          <ul>
            <li><a href="#empi">EMPI</a></li>
            <li><a href="#dpi">DPI</a></li>
            <li><a href="#cpoe">CPOE</a></li>
            <li><a href="#lis">LIS</a></li>
            <li><a href="#ris">RIS</a></li>
            <li><a href="#pharmacy">Pharmacie</a></li>
          </ul>
        </nav>
      </header>
      <main>
        <section id="status">
          <h2>État du système</h2>
          <p>Tous les services sont en cours de développement...</p>
        </section>
      </main>
    </div>
  );
}

export default App;
