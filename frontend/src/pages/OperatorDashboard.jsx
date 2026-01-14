
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { LogOut, Car, LayoutDashboard, Phone, User, RefreshCw, X } from 'lucide-react';
import parkService from '../services/parkService';
import sessionService from '../services/sessionService';
import authService from '../services/authService';
import utilizadorService from '../services/utilizadorService';

const OperatorDashboard = () => {
    const [parks, setParks] = useState([]);
    const [selectedParkId, setSelectedParkId] = useState(null);
    const [sessions, setSessions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false); // Visual feedback
    const [contactInfo, setContactInfo] = useState(null);
    const [currentUser, setCurrentUser] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        const user = JSON.parse(localStorage.getItem('user'));
        if (!user || user.role !== 'OPERADOR') {

        }
        setCurrentUser(user);
        fetchParks();
    }, []);

    useEffect(() => {
        let interval;
        if (selectedParkId) {
            fetchSessions(selectedParkId);
            interval = setInterval(() => fetchSessions(selectedParkId), 5000);
        }
        return () => clearInterval(interval);
    }, [selectedParkId]);

    const fetchParks = async () => {
        try {
            const data = await parkService.getParks();
            setParks(data.content || data);
            if (data.length > 0 || (data.content && data.content.length > 0)) {
                const list = data.content || data;
                if (list.length > 0) setSelectedParkId(list[0].id);
            }
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const fetchSessions = async (parkId) => {
        setRefreshing(true);
        try {
            const res = await sessionService.getActiveByParque(parkId);
            setSessions(res.data || []);
        } catch (err) {
            console.error(err);
        } finally {
            setTimeout(() => setRefreshing(false), 500); // Min spin time
        }
    };

    const handleContact = async (matricula) => {
        try {
            const res = await utilizadorService.searchByMatricula(matricula);
            setContactInfo({ matricula, ...res.data });
        } catch (err) {
            alert('Utilizador não encontrado ou erro de acesso');
        }
    };

    const handleStop = async (sessionId) => {
        if (!window.confirm('Tem a certeza que deseja terminar forçadamente esta sessão?')) return;
        try {
            await sessionService.stopSession(sessionId);
            fetchSessions(selectedParkId);
        } catch (err) {
            alert('Erro ao terminar sessão');
        }
    };

    const formatTime = (isoString) => {
        return new Date(isoString).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    };

    return (
        <div className="dashboard-container">
            <aside className="sidebar glass">
                <div className="logo">
                    <Car size={32} className="logo-icon" />
                    <span>PRR Operator</span>
                </div>
                <nav>
                    <button className="active">
                        <LayoutDashboard size={20} /> Monitorização
                    </button>
                    {/* Add more tabs if needed */}
                </nav>
                <div className="sidebar-footer">
                    <div className="user-info-mini">
                        <User size={16} /> {currentUser?.nome || 'Operador'}
                    </div>
                    <button className="logout-btn" onClick={() => { authService.logout(); navigate('/login'); }}>
                        <LogOut size={20} /> Sair
                    </button>
                </div>
            </aside>

            <main className="content">
                <header className="main-header">
                    <div>
                        <h1>Monitor de Parque</h1>
                        <p>Gestão de acesso e fiscalização em tempo real</p>
                    </div>
                    <div>
                        <select
                            className="park-select glass"
                            value={selectedParkId || ''}
                            onChange={(e) => setSelectedParkId(Number(e.target.value))}
                        >
                            {parks.map(p => <option key={p.id} value={p.id}>{p.nome}</option>)}
                        </select>
                    </div>
                </header>

                <div className="monitor-grid">
                    <div className="monitor-card glass">
                        <div className="card-header-row">
                            <h3>Sessões Ativas ({sessions.length})</h3>
                            <RefreshCw size={18} className={`spin-hover ${refreshing ? 'spin' : ''}`} onClick={() => fetchSessions(selectedParkId)} />
                        </div>

                        <div className="table-container">
                            <div className="table-header">
                                <span>Matrícula</span>
                                <span>Início</span>
                                <span>Duração (aprox)</span>
                                <span>Ações</span>
                            </div>
                            {sessions.length === 0 ? (
                                <div className="empty-state">Sem viaturas no parque.</div>
                            ) : (
                                sessions.map(s => {
                                    const duration = Math.floor((new Date() - new Date(s.inicio)) / 1000 / 60);
                                    return (
                                        <div key={s.sessaoId} className="table-row">
                                            <span className="plate-badge">{s.matricula}</span>
                                            <span>{formatTime(s.inicio)}</span>
                                            <span>{duration} min</span>
                                            <div className="actions">
                                                <button className="btn-icon" onClick={() => handleContact(s.matricula)} title="Contactar">
                                                    <Phone size={18} />
                                                </button>
                                                <button className="btn-icon danger" onClick={() => handleStop(s.sessaoId)} title="Terminar Sessão">
                                                    <LogOut size={18} />
                                                </button>
                                            </div>
                                        </div>
                                    );
                                })
                            )}
                        </div>
                    </div>
                </div>
            </main>

            {contactInfo && (
                <div className="modal-overlay">
                    <div className="modal-content glass animate-pop">
                        <button className="close-modal" onClick={() => setContactInfo(null)}><X size={24} /></button>
                        <h2>Dados do Condutor</h2>
                        <div className="contact-details">
                            <div className="detail-row">
                                <label>Matrícula</label>
                                <strong>{contactInfo.matricula}</strong>
                            </div>
                            <div className="detail-row">
                                <label>Nome</label>
                                <h3>{contactInfo.nome}</h3>
                            </div>
                            <div className="detail-row">
                                <label>Telemóvel</label>
                                <h1 className="phone-number">{contactInfo.telemovel}</h1>
                            </div>
                            <a href={`tel:${contactInfo.telemovel}`} className="call-btn">
                                <Phone size={20} /> Ligar Agora
                            </a>
                        </div>
                    </div>
                </div>
            )}

            <style>{`
                /* Operator Theme Override */
                :root { --primary: #00e5ff; --primary-glow: rgba(0, 229, 255, 0.3); }

                /* Specifics */
                .park-select { padding: 10px 20px; font-size: 1rem; color: white; background: #1a1b26; border: 1px solid #334; border-radius: 8px; outline: none; }
                
                .monitor-grid { max-width: 1000px; margin: 0 auto; }
                .monitor-card { padding: 30px; min-height: 500px; }
                .card-header-row { display: flex; justify-content: space-between; margin-bottom: 25px; align-items: center; }
                
                .table-container { display: flex; flex-direction: column; gap: 10px; }
                .table-header { display: grid; grid-template-columns: 1.5fr 1fr 1fr 1fr; padding: 0 15px; color: #667; font-size: 0.85rem; font-weight: 700; text-transform: uppercase; margin-bottom: 10px; }
                .table-row { 
                    display: grid; grid-template-columns: 1.5fr 1fr 1fr 1fr; padding: 15px; 
                    background: rgba(255,255,255,0.03); border-radius: 10px; align-items: center; transition: 0.2s;
                }
                .table-row:hover { background: rgba(255,255,255,0.06); transform: translateX(5px); }
                
                .plate-badge { background: white; color: black; padding: 4px 8px; border-radius: 4px; font-family: monospace; font-weight: 800; border: 2px solid #333; }
                .actions { display: flex; gap: 10px; }
                .btn-icon { padding: 8px; border-radius: 8px; border: none; background: rgba(255,255,255,0.1); color: white; cursor: pointer; transition: 0.2s; }
                .btn-icon:hover { background: var(--primary); color: black; }
                .btn-icon.danger { color: #ff1744; }
                .btn-icon.danger:hover { background: #ff1744; color: white; }
                
                .contact-details { margin-top: 30px; display: flex; flex-direction: column; gap: 20px; }
                .detail-row label { display: block; font-size: 0.8rem; color: #667; margin-bottom: 5px; }
                .phone-number { color: #00e5ff; font-family: monospace; letter-spacing: 1px; }
                .call-btn { 
                    margin-top: 20px; display: flex; align-items: center; justify-content: center; gap: 10px;
                    padding: 15px; background: #00e676; color: black; font-weight: 700; text-decoration: none; border-radius: 10px;
                }
                
                .user-info-mini { margin-bottom: 15px; display: flex; align-items: center; gap: 8px; font-size: 0.9rem; color: #889; }

                @media (max-width: 768px) {
                    .monitor-grid { padding: 10px; }
                    .table-header { display: none; } /* Hide header on mobile */
                    .table-row { 
                        display: flex; 
                        flex-direction: column; 
                        gap: 10px; 
                        align-items: flex-start;
                        position: relative;
                    }
                    .table-row span { font-size: 1rem; }
                    .table-row .actions { 
                        width: 100%; 
                        justify-content: flex-end; 
                        margin-top: 10px; 
                        border-top: 1px solid rgba(255,255,255,0.1); 
                        padding-top: 10px; 
                    }
                    .btn-icon { padding: 12px; } /* Larger touch target */
                }
            `}</style>
        </div>
    );
};

export default OperatorDashboard;
