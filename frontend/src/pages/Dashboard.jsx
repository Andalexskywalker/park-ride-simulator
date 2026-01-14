import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import parkService from '../services/parkService';
import authService from '../services/authService';
import utilizadorService from '../services/utilizadorService';
import sessionService from '../services/sessionService';
import { LogOut, Car, LayoutDashboard, TrendingUp, Loader2, MapPin, Activity, Clock, Plus, User, X, Receipt } from 'lucide-react';

import Analytics from './Analytics';
import OperatorDashboard from './OperatorDashboard';
import AdminDashboard from './AdminDashboard';

const Dashboard = () => {
  const [parks, setParks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [simulating, setSimulating] = useState(false);
  const [activeSession, setActiveSession] = useState(null);
  const [matricula, setMatricula] = useState('');
  const [activeTab, setActiveTab] = useState('dashboard');
  const [timer, setTimer] = useState(0);
  const [viaturas, setViaturas] = useState([]);
  const [currentUser, setCurrentUser] = useState(null);
  const [newViatura, setNewViatura] = useState({ matricula: '', marca: '', modelo: '' });
  const [invoices, setInvoices] = useState([]);
  const [paymentSummary, setPaymentSummary] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    fetchParks();
    fetchViaturas();
  }, []);

  useEffect(() => {
    let interval;
    if (activeSession) {
      interval = setInterval(() => {
        setTimer(prev => prev + 1);
      }, 1000);
    } else {
      setTimer(0);
    }
    return () => clearInterval(interval);
  }, [activeSession]);

  useEffect(() => {
    const user = JSON.parse(localStorage.getItem('user'));
    setCurrentUser(user);
    if (user?.role !== 'ADMIN' && activeTab === 'analytics') setActiveTab('dashboard');
    if (activeTab === 'profile') fetchInvoices();
  }, [activeTab, viaturas]);

  useEffect(() => {
    if (matricula.length >= 6) checkActiveSession(matricula);
  }, [matricula]);

  const checkActiveSession = async (mat) => {
    try {
      const res = await sessionService.getActiveSession(mat);
      if (res.data) {
        // Calcular offset do timer com base no inicio do backend
        const startTime = new Date(res.data.inicio).getTime();
        const now = new Date().getTime();
        const elapsed = Math.floor((now - startTime) / 1000);
        setTimer(elapsed > 0 ? elapsed : 0);
        setActiveSession({ ...res.data, parkName: res.data.parkName || 'Parque Desconhecido' });
      }
    } catch (err) {
      if (err.response && err.response.status !== 404) {
        console.error("Erro ao verificar sessão ativa", err);
      }
    }
  };

  const formatPlate = (value) => {
    const v = value.toUpperCase().replace(/[^A-Z0-9]/g, '');
    if (v.length > 6) return `${v.slice(0, 2)}-${v.slice(2, 4)}-${v.slice(4, 8)}`; // Moderno?
    if (v.length > 4) return `${v.slice(0, 2)}-${v.slice(2, 4)}-${v.slice(4, 6)}`;
    if (v.length > 2) return `${v.slice(0, 2)}-${v.slice(2, 4)}`;
    return v;
  };

  const fetchParks = async () => {
    try {
      const data = await parkService.getParks();
      setParks(data.content || data);
    } catch (err) {
      console.error('Erro ao carregar parques', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchViaturas = async () => {
    try {
      const user = JSON.parse(localStorage.getItem('user'));
      if (user && user.id) {
        const data = await utilizadorService.getViaturas(user.id);
        setViaturas(data || []);
        // Se tiver viatura, pre-fill matricula e check active session
        if (data.length > 0 && !matricula) {
          setMatricula(data[0].matricula);
        }
      }
    } catch (err) {
      console.error('Erro ao carregar viaturas', err);
    }
  };

  const handleAddViatura = async (e) => {
    e.preventDefault();
    try {
      const user = JSON.parse(localStorage.getItem('user'));
      if (!user || !user.id) return;

      await utilizadorService.addViatura(user.id, newViatura);
      alert('Viatura adicionada com sucesso!');
      setNewViatura({ matricula: '', marca: '', modelo: '' });
      fetchViaturas();
    } catch (err) {
      alert(err.response?.data?.message || 'Erro ao adicionar viatura');
    }
  };

  const handleStartSession = async (parkId) => {
    if (!matricula) return alert('Insira uma matrícula (ex: AA-00-AA)');
    setSimulating(true);
    try {
      const res = await sessionService.startSession(matricula, parkId);
      setActiveSession({ ...res.data, parkId, parkName: parks.find(p => p.id === parkId)?.nome });
      fetchParks();
    } catch (err) {
      console.error(err);
      const serverMsg = err.response?.data?.message;
      const techErr = err.toJSON ? JSON.stringify(err.toJSON()) : err.message;
      alert(`Server: ${serverMsg} || Tech: ${techErr}`);
    } finally {
      setSimulating(false);
    }
  };

  const fetchInvoices = async () => {
    if (!viaturas || viaturas.length === 0) return;
    try {
      const allInvoices = [];
      for (const v of viaturas) {
        const res = await sessionService.getInvoices(v.matricula);
        if (res.data) allInvoices.push(...res.data);
      }
      // Sort by id desc (proxy for date)
      setInvoices(allInvoices.sort((a, b) => b.id - a.id));
    } catch (err) {
      console.error("Erro ao carregar faturas", err);
    }
  };

  const handleStopSession = async () => {
    if (!activeSession) return;
    setSimulating(true);
    try {
      const res = await sessionService.stopSession(activeSession.sessaoId);

      setPaymentSummary({
        total: res.data.total,
        sessaoId: activeSession.sessaoId,
        matricula: activeSession.matricula,
        parkName: activeSession.parkName,
        tempo: formatTime(timer)
      });
      setActiveSession(null);
      fetchParks();
    } catch (err) {
      alert('Erro ao terminar sessão.');
    } finally {
      setSimulating(false);
    }
  };

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  if (loading) return <div className="app-loader"><Loader2 className="spin" /></div>;

  if (currentUser?.role === 'OPERADOR') return <OperatorDashboard />;
  if (currentUser?.role === 'ADMIN') return <AdminDashboard />;

  return (
    <div className="dashboard-container">
      <aside className="sidebar glass">
        <div className="logo">
          <Car size={32} className="logo-icon" />
          <span>PRR Simulator</span>
        </div>

        <nav>
          <button className={activeTab === 'dashboard' ? 'active' : ''} onClick={() => setActiveTab('dashboard')}>
            <LayoutDashboard size={20} /> Dashboard
          </button>
          <button className={activeTab === 'simulator' ? 'active' : ''} onClick={() => setActiveTab('simulator')}>
            <Activity size={20} /> Simulador
          </button>
          <button className={activeTab === 'viaturas' ? 'active' : ''} onClick={() => setActiveTab('viaturas')}>
            <Car size={20} /> Minhas Viaturas
          </button>

          {currentUser?.role === 'ADMIN' && (
            <button className={activeTab === 'analytics' ? 'active' : ''} onClick={() => setActiveTab('analytics')}>
              <TrendingUp size={20} /> Analytics
            </button>
          )}

          <button className={activeTab === 'profile' ? 'active' : ''} onClick={() => setActiveTab('profile')}>
            <User size={20} /> Minha Conta
          </button>
        </nav>

        <div className="sidebar-footer">
          <button className="logout-btn" onClick={() => { authService.logout(); navigate('/login'); }}>
            <LogOut size={20} /> Sair
          </button>
        </div>
      </aside>

      <main className="content">
        {activeTab === 'dashboard' && (
          <div className="fadeIn">
            <header className="main-header">
              <div>
                <h1>Estado da Rede</h1>
                <p>Visão em tempo real da ocupação dos parques municipais</p>
              </div>
              <div className="status-badge glass online">SISTEMA ATIVO</div>
            </header>

            <div className="stats-grid">
              <div className="stat-card glass luxury">
                <span className="label">Ocupação Global</span>
                <div className="value-row">
                  <h3>{parks.length > 0 ? Math.round(parks.reduce((acc, p) => acc + (p.ocupacaoAtual / p.capacidadeTotal), 0) / parks.length * 100) : 0}%</h3>
                  <TrendingUp size={24} className="trend-icon" />
                </div>
              </div>
              <div className="stat-card glass">
                <span className="label">Total de Parques</span>
                <h3>{parks.length}</h3>
              </div>
              <div className="stat-card glass">
                <span className="label">Lugares Livres</span>
                <h3>{parks.reduce((acc, p) => acc + (p.capacidadeTotal - p.ocupacaoAtual), 0)}</h3>
              </div>
            </div>

            <section className="parks-section">
              <div className="section-header">
                <h2>Inventário de Parques</h2>
              </div>
              <div className="parks-grid">
                {parks.map(park => (
                  <div key={park.id} className="park-card glass hover-lift">
                    <div className="park-info">
                      <h3>{park.nome}</h3>
                      <p><MapPin size={14} /> {park.cidade} • <strong>{park.precoHora}€/h</strong></p>
                    </div>
                    <div className="occupation-meter">
                      <div className="meter-header">
                        <span>Lotação</span>
                        <span className={(park.ocupacaoAtual / park.capacidadeTotal) > 0.8 ? 'alert' : ''}>
                          {park.ocupacaoAtual}/{park.capacidadeTotal}
                        </span>
                      </div>
                      <div className="meter-bar">
                        <div
                          className="meter-fill"
                          style={{
                            width: `${(park.ocupacaoAtual / park.capacidadeTotal) * 100}%`,
                            background: (park.ocupacaoAtual / park.capacidadeTotal) > 0.9 ? 'var(--danger)' : 'linear-gradient(90deg, var(--primary), var(--secondary))'
                          }}
                        ></div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </section>
          </div>
        )}

        {activeTab === 'simulator' && (
          <div className="simulator-view fadeIn">
            <header className="main-header">
              <div>
                <h1>Simulador de Fluxo</h1>
                <p>Gestão interativa de acessos e sessões de estacionamento</p>
              </div>
            </header>

            <div className="simulator-layout">
              <div className="sim-panel-left">
                <div className="vehicle-entry-card glass highlight">
                  <div className="card-header">
                    <Car size={24} />
                    <h3>Registo de Entrada</h3>
                  </div>

                  <div className="license-plate-container">
                    <div className="plate-base">
                      <div className="plate-blue-strip">P</div>
                      <input
                        className="plate-input"
                        placeholder="AA-00-AA"
                        maxLength={8}
                        value={matricula}
                        onChange={(e) => setMatricula(formatPlate(e.target.value))}
                      />
                    </div>
                    <p className="hint">Formato: Letras e Números (ex: 12-XY-34)</p>
                  </div>

                  {activeSession && (
                    <div className="active-session-box animate-pop">
                      <div className="session-info">
                        <div className="session-header">
                          <Clock size={16} />
                          <span>Estacionamento em Curso</span>
                        </div>
                        <h4>{activeSession.parkName}</h4>
                        <div className="timer-display">{formatTime(timer)}</div>
                        <p className="subtext">Para a matrícula: <strong>{activeSession.matricula}</strong></p>
                      </div>
                      <button className="stop-btn-luxury" onClick={handleStopSession} disabled={simulating}>
                        {simulating ? <Loader2 className="spin" /> : 'CONCLUIR E PAGAR'}
                      </button>
                    </div>
                  )}
                </div>
              </div>

              <div className="sim-panel-right">
                <div className="parks-selection-header">
                  <h3>Escolha o Parque de Destino</h3>
                  <p>Apenas parques com lugares disponíveis</p>
                </div>

                <div className="sim-parks-grid">
                  {parks.map(park => (
                    <div key={park.id} className={`sim-park-card glass ${activeSession ? 'disabled' : ''}`}>
                      <div className="park-meta">
                        <h4>{park.nome}</h4>
                        <span className="capacity-badge">
                          {park.capacidadeTotal - park.ocupacaoAtual} lugares
                        </span>
                      </div>
                      <button
                        className="sim-action-btn"
                        onClick={() => handleStartSession(park.id)}
                        disabled={simulating || activeSession || park.ocupacaoAtual >= park.capacidadeTotal}
                      >
                        {simulating ? <Loader2 className="spin" /> : 'ENTRAR NO PARQUE'}
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'analytics' && <Analytics />}

        {activeTab === 'viaturas' && (
          <div className="fadeIn">
            <header className="main-header">
              <div>
                <h1>Minhas Viaturas</h1>
                <p>Gerir frota e registar novos veículos</p>
              </div>
            </header>

            <div className="viaturas-layout">
              <div className="add-vehicle-card glass">
                <h3>Adicionar Viatura</h3>
                <form onSubmit={handleAddViatura} className="vehicle-form">
                  <input
                    placeholder="Matrícula (AA-00-AA)"
                    value={newViatura.matricula}
                    onChange={e => setNewViatura({ ...newViatura, matricula: e.target.value.toUpperCase() })}
                    maxLength={8}
                    required
                  />
                  <input
                    placeholder="Marca"
                    value={newViatura.marca}
                    onChange={e => setNewViatura({ ...newViatura, marca: e.target.value })}
                    required
                  />
                  <input
                    placeholder="Modelo"
                    value={newViatura.modelo}
                    onChange={e => setNewViatura({ ...newViatura, modelo: e.target.value })}
                    required
                  />
                  <button type="submit" className="add-btn">
                    <Plus size={18} /> Registar
                  </button>
                </form>
              </div>

              <div className="viaturas-grid-list">
                {viaturas.map(v => (
                  <div key={v.id} className="viatura-card glass hover-lift">
                    <div className="plate-display-mini">{v.matricula}</div>
                    <div className="viatura-info">
                      <h4>{v.marca}</h4>
                      <p>{v.modelo}</p>
                    </div>
                  </div>
                ))}
                {viaturas.length === 0 && <p className="no-data">Nenhuma viatura registada.</p>}
              </div>
            </div>
          </div>
        )}

        {activeTab === 'profile' && (
          <div className="fadeIn">
            <header className="main-header">
              <div>
                <h1>A Minha Conta</h1>
                <p>Detalhes do perfil de utilizador</p>
              </div>
            </header>
            <div className="metric-card glass highlight" style={{ maxWidth: '500px' }}>
              <div className="metric-info">
                <span>Nome</span>
                <h3>{currentUser?.nome}</h3>
              </div>
              <div className="metric-info" style={{ marginTop: '20px' }}>
                <span>Email</span>
                <h3>{currentUser?.email}</h3>
              </div>
              <div className="metric-info" style={{ marginTop: '20px' }}>
                <span>Role</span>
                <h3>{currentUser?.role}</h3>
              </div>
              <div style={{ marginTop: '30px', padding: '15px', background: 'rgba(255,255,255,0.05)', borderRadius: '8px' }}>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>Para alterar a sua password, contacte o administrador (Funcionalidade em desenvolvimento).</p>
              </div>
            </div>

            <div className="section-header" style={{ marginTop: '50px' }}>
              <h2>Histórico de Pagamentos</h2>
            </div>

            <div className="invoices-table glass">
              <div className="table-header">
                <span>ID Fat.</span>
                <span>Matrícula</span>
                <span>Valor</span>
              </div>
              {invoices.length === 0 ? (
                <div className="table-row empty"><p>Sem histórico de pagamentos.</p></div>
              ) : (
                invoices.map(inv => (
                  <div key={inv.id} className="table-row">
                    <span>#{inv.id}</span>
                    <span className="mono">{inv.matricula}</span>
                    <span className="value">{inv.valor} €</span>
                  </div>
                ))
              )}
            </div>
          </div>
        )}

        {paymentSummary && (
          <div className="modal-overlay">
            <div className="modal-content glass animate-pop">
              <button className="close-modal" onClick={() => setPaymentSummary(null)}><X size={24} /></button>
              <div className="receipt-header">
                <div className="check-icon"><Receipt size={32} /></div>
                <h2>Pagamento Confirmado</h2>
                <p>Obrigado por utilizar o PRR Park & Ride</p>
              </div>

              <div className="receipt-details">
                <div className="receipt-row">
                  <span>Parque</span>
                  <strong>{paymentSummary.parkName}</strong>
                </div>
                <div className="receipt-row">
                  <span>Matrícula</span>
                  <strong className="mono">{paymentSummary.matricula}</strong>
                </div>
                <div className="receipt-row">
                  <span>Duração</span>
                  <strong>{paymentSummary.tempo}</strong>
                </div>
                <div className="receipt-divider"></div>
                <div className="receipt-total">
                  <span>Total Pago</span>
                  <h1>{paymentSummary.total} €</h1>
                </div>
              </div>

              <button className="receipt-btn" onClick={() => setPaymentSummary(null)}>
                Fechar Recibo
              </button>
            </div>
          </div>
        )}
      </main>

      <style>{`
        /* Dashboard Specifics */
        .status-badge { padding: 8px 16px; border-radius: 20px; font-size: 0.75rem; font-weight: 700; letter-spacing: 1px; }
        .status-badge.online { color: #00e676; border: 1px solid rgba(0, 230, 118, 0.3); background: rgba(0, 230, 118, 0.1); }

        .stat-card.luxury { border-bottom: 3px solid var(--primary); }
        .trend-icon { color: var(--primary); }

        /* Park Cards */
        .parks-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 30px; }
        .park-card { padding: 30px; }
        .park-info h3 { font-size: 1.4rem; margin-bottom: 15px; }
        .park-info p { display: flex; align-items: center; gap: 8px; color: var(--text-muted); font-size: 0.9rem; margin-bottom: 25px; }
        
        .meter-bar { height: 10px; background: rgba(255,255,255,0.05); border-radius: 5px; overflow: hidden; margin-top: 5px; }
        .meter-fill { height: 100%; border-radius: 5px; transition: width 0.6s; }

        /* Simulator Plate */
        .simulator-layout { display: grid; grid-template-columns: 1fr 1.5fr; gap: 40px; margin-top: 20px; }
        .vehicle-entry-card { padding: 40px; position: sticky; top: 20px; }
        
        .plate-base { 
            background: white; border-radius: 6px; border: 4px solid #333; display: flex; height: 60px;
            overflow: hidden; width: 260px; margin: 0 auto 15px; box-shadow: 0 5px 15px rgba(0,0,0,0.3);
        }
        .plate-blue-strip { width: 30px; background: #003399; color: white; display: flex; align-items: center; justify-content: center; font-weight: 700; }
        .plate-input { 
            flex: 1; border: none; background: transparent; color: #333; font-family: 'Consolas', monospace;
            font-weight: 700; font-size: 1.8rem; text-align: center; letter-spacing: 2px; text-transform: uppercase;
        }

        .active-session-box { margin-top: 40px; padding: 30px; background: rgba(0, 229, 255, 0.05); border: 1px dashed var(--primary); border-radius: 16px; }
        .timer-display { font-size: 3.5rem; font-weight: 800; color: white; font-family: monospace; margin: 10px 0; }
        
        .stop-btn-luxury { 
            width: 100%; padding: 16px; background: var(--danger); color: white; border: none; border-radius: 12px;
            font-weight: 800; margin-top: 25px; cursor: pointer; transition: 0.3s;
        }
        .stop-btn-luxury:hover { background: #ff1744; transform: scale(1.02); }

        .sim-parks-grid { display: grid; grid-template-columns: 1fr; gap: 15px; margin-top: 25px; }
        .sim-park-card { padding: 25px; display: flex; justify-content: space-between; align-items: center; cursor: pointer; border: 1px solid transparent; transition: 0.3s; }
        .sim-park-card:hover:not(.disabled) { border-color: var(--primary); background: rgba(255,255,255,0.05); }
        .sim-park-card.disabled { opacity: 0.5; pointer-events: none; }
        
        .capacity-badge { background: rgba(255,255,255,0.05); padding: 5px 12px; border-radius: 20px; font-size: 0.8rem; color: var(--secondary); margin-top: 5px; display: block; width: fit-content; }

        /* Viaturas */
        .viaturas-layout { display: grid; grid-template-columns: 1fr 2fr; gap: 40px; }
        .viaturas-grid-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 20px; }
        .viatura-card { padding: 20px; display: flex; flex-direction: column; align-items: center; text-align: center; gap: 15px; }
        .plate-display-mini { background: white; color: black; padding: 5px 10px; border: 2px solid #333; border-radius: 4px; font-weight: 800; font-family: monospace; }
        
        .invoices-table { width: 100%; max-width: 800px; margin-top: 20px; }
        .invoices-table .table-header { grid-template-columns: 1fr 2fr 1fr; }
        .invoices-table .table-row { grid-template-columns: 1fr 2fr 1fr; }
        
        .check-icon { width: 60px; height: 60px; background: var(--primary); border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 20px; color: white; }
        .receipt-details { background: rgba(255,255,255,0.03); padding: 20px; border-radius: 12px; margin-bottom: 25px; }
        .receipt-row { display: flex; justify-content: space-between; margin-bottom: 15px; font-size: 0.9rem; color: var(--text-muted); }
        
        /* FIX: Simulator & Vehicle Form Styles */
        .vehicle-form { display: flex; flex-direction: column; gap: 10px; }
        .add-btn { width: 100%; padding: 12px; background: var(--success); color: #000; font-weight: 700; border-radius: 8px; display: flex; justify-content: center; gap: 8px; margin-top: 10px; }
        .add-btn:hover { filter: brightness(1.1); }

        .sim-action-btn { 
            width: 100%; padding: 12px; background: var(--primary); color: white; font-weight: 700; border-radius: 8px; 
            font-size: 0.8rem; letter-spacing: 0.5px; margin-top: 15px; 
        }
        .sim-action-btn:disabled { opacity: 0.5; background: #333; cursor: not-allowed; }
        .sim-action-btn:hover:not(:disabled) { background: #651fff; box-shadow: 0 5px 15px var(--primary-glow); }

        /* Responsive Design */
        @media (max-width: 1000px) {
            .sidebar { width: 80px; padding: 20px 10px; }
            .sidebar .logo span, .sidebar nav button span, .sidebar nav button text, .sidebar-footer button span { display: none; }
            .sidebar .logo { justify-content: center; margin-bottom: 30px; }
            .sidebar nav button, .sidebar-footer button { padding: 12px; justify-content: center; }
            
            .content { padding: 20px; }
            .simulator-layout { grid-template-columns: 1fr; }
            .viaturas-layout { grid-template-columns: 1fr; }
            .vehicle-entry-card { position: static; }
            
            .main-header { flex-direction: column; gap: 15px; }
            .parks-grid { grid-template-columns: 1fr; }
            
            /* Compact Plate for mobile */
            .plate-base { width: 100%; max-width: 260px; }
        }
      `}</style>
    </div>
  );
};

export default Dashboard;
