
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { LogOut, LayoutDashboard, User, Shield, UserPlus, Search, X, Check, Loader2 } from 'lucide-react';
import authService from '../services/authService';
import utilizadorService from '../services/utilizadorService';

const AdminDashboard = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [selectedUser, setSelectedUser] = useState(null); // For details modal
    const [pendingOperator, setPendingOperator] = useState(null); // For creation modal
    const [formError, setFormError] = useState('');
    const [successMsg, setSuccessMsg] = useState('');
    const navigate = useNavigate();

    const [newOp, setNewOp] = useState({
        nome: '',
        email: '',
        password: '',
        telemovel: '',
        nif: ''
    });

    useEffect(() => {
        fetchUsers();
    }, []);

    const fetchUsers = async () => {
        try {
            setLoading(true);
            const res = await utilizadorService.getAll();
            // Handle pagination if present (Spring Data returns .content)
            setUsers(res.data.content || res.data || []);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleCreateOperator = async (e) => {
        e.preventDefault();
        setFormError('');
        try {
            await utilizadorService.createOperator({
                ...newOp,
                role: 'OPERADOR' // Backend sets this, but implicit here
            });
            setSuccessMsg('Operador criado com sucesso!');
            setPendingOperator(null);
            setNewOp({ nome: '', email: '', password: '', telemovel: '', nif: '' });
            fetchUsers();
            setTimeout(() => setSuccessMsg(''), 3000);
        } catch (err) {
            setFormError(err.response?.data?.message || 'Erro ao criar operador');
        }
    };

    const filteredUsers = users.filter(u =>
        (u.nome || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
        (u.email || '').toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="dashboard-container">
            <aside className="sidebar glass">
                <div className="logo">
                    <Shield size={32} className="logo-icon admin-icon" />
                    <span>PRR Admin</span>
                </div>
                <nav>
                    <button className="active">
                        <LayoutDashboard size={20} /> Gestão de Utilizadores
                    </button>

                </nav>
                <div className="sidebar-footer">
                    <button className="logout-btn" onClick={() => { authService.logout(); navigate('/login'); }}>
                        <LogOut size={20} /> Sair
                    </button>
                </div>
            </aside>

            <main className="content">
                <header className="main-header">
                    <div>
                        <h1>Gestão de Acessos</h1>
                        <p>Plataforma administrativa central</p>
                    </div>
                    <button className="btn-primary" onClick={() => setPendingOperator(true)}>
                        <UserPlus size={20} /> Novo Operador
                    </button>
                </header>

                {successMsg && <div className="success-banner animate-pop"><Check size={20} /> {successMsg}</div>}

                <div className="table-card glass">
                    <div className="card-header-row">
                        <h3>Utilizadores Registados ({filteredUsers.length})</h3>
                        <div className="search-box">
                            <Search size={18} />
                            <input
                                placeholder="Pesquisar utilizador..."
                                value={searchTerm}
                                onChange={e => setSearchTerm(e.target.value)}
                            />
                        </div>
                    </div>

                    <div className="table-container">
                        <div className="table-header">
                            <span>Nome</span>
                            <span>Email</span>
                            <span>Role</span>
                            <span>Contacto</span>
                            <span>Ações</span>
                        </div>
                        {loading ? <div className="loading-row"><Loader2 className="spin" /></div> : (
                            filteredUsers.length === 0 ? <p className="empty-state">Nenhum utilizador encontrado.</p> :
                                filteredUsers.map(u => (
                                    <div key={u.id} className="table-row hover-lift-subtle">
                                        <span className="user-name"><User size={16} /> {u.nome}</span>
                                        <span className="mono-sm">{u.email}</span>
                                        <span>
                                            <span className={`role-badge ${u.role.toLowerCase()}`}>{u.role}</span>
                                        </span>
                                        <span className="mono-sm">{u.telemovel}</span>
                                        <div className="actions">
                                            <button className="btn-text" onClick={() => setSelectedUser(u)}>Detalhes</button>
                                        </div>
                                    </div>
                                ))
                        )}
                    </div>
                </div>
            </main>

            {pendingOperator && (
                <div className="modal-overlay">
                    <div className="modal-content glass animate-pop">
                        <button className="close-modal" onClick={() => setPendingOperator(null)}><X size={24} /></button>
                        <h2>Registar Operador</h2>
                        <p className="modal-subtitle">Crie uma conta para gestão de parque.</p>

                        <form onSubmit={handleCreateOperator} className="admin-form">
                            <div className="form-group">
                                <label>Nome Completo</label>
                                <input required value={newOp.nome} onChange={e => setNewOp({ ...newOp, nome: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label>Email Institucional</label>
                                <input type="email" required value={newOp.email} onChange={e => setNewOp({ ...newOp, email: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label>Password Inicial</label>
                                <input type="password" required value={newOp.password} onChange={e => setNewOp({ ...newOp, password: e.target.value })} />
                            </div>
                            <div className="row-2">
                                <div className="form-group">
                                    <label>Telemóvel</label>
                                    <input required value={newOp.telemovel} onChange={e => setNewOp({ ...newOp, telemovel: e.target.value })} />
                                </div>
                                <div className="form-group">
                                    <label>NIF</label>
                                    <input required value={newOp.nif} onChange={e => setNewOp({ ...newOp, nif: e.target.value })} />
                                </div>
                            </div>

                            {formError && <p className="error-msg">{formError}</p>}

                            <button type="submit" className="submit-btn btn-primary" style={{ width: '100%', justifyContent: 'center' }}>Criar Conta</button>
                        </form>
                    </div>
                </div>
            )}

            {selectedUser && (
                <div className="modal-overlay">
                    <div className="modal-content glass animate-pop">
                        <button className="close-modal" onClick={() => setSelectedUser(null)}><X size={24} /></button>
                        <h2>Detalhes do Utilizador</h2>
                        <p className="modal-subtitle">Informação completa da conta.</p>

                        <div className="user-details-grid" style={{ marginTop: '20px' }}>
                            <div className="detail-item">
                                <label>Nome</label>
                                <p>{selectedUser.nome}</p>
                            </div>
                            <div className="detail-item">
                                <label>Email</label>
                                <p>{selectedUser.email}</p>
                            </div>
                            <div className="detail-item">
                                <label>NIF</label>
                                <p>{selectedUser.nif}</p>
                            </div>
                            <div className="detail-item">
                                <label>Telemóvel</label>
                                <p>{selectedUser.telemovel || 'N/A'}</p>
                            </div>
                            <div className="detail-item">
                                <label>Função</label>
                                <span className={`role-badge ${selectedUser.role.toLowerCase()}`}>{selectedUser.role}</span>
                            </div>
                        </div>

                        <div style={{ marginTop: '30px', borderTop: '1px solid var(--glass-border)', paddingTop: '20px' }}>
                            <button className="btn-primary" style={{ width: '100%', justifyContent: 'center' }} onClick={() => setSelectedUser(null)}>Fechar</button>
                        </div>
                    </div>
                </div>
            )}

            <style>{`
                /* Admin Specifics */
                .card-header-row { display: flex; justify-content: space-between; padding: 20px; align-items: center; border-bottom: 1px solid var(--glass-border); }
                .search-box { display: flex; align-items: center; gap: 10px; background: rgba(255,255,255,0.05); padding: 8px 15px; border-radius: 8px; border: 1px solid var(--glass-border); }
                .search-box input { background: transparent; border: none; padding: 0; height: auto; }
                .search-box input:focus { box-shadow: none; }
                
                .table-container { padding: 0; }
                .table-header { display: grid; grid-template-columns: 1.5fr 2fr 1fr 1fr 1fr; padding: 15px 20px; background: rgba(255,255,255,0.02); color: var(--text-muted); font-size: 0.85rem; font-weight: 700; text-transform: uppercase; }
                .table-row { display: grid; grid-template-columns: 1.5fr 2fr 1fr 1fr 1fr; padding: 15px 20px; align-items: center; border-bottom: 1px solid var(--glass-border); transition: 0.2s; }
                .table-row:hover { background: rgba(255,255,255,0.03); }
                
                .user-name { display: flex; align-items: center; gap: 10px; font-weight: 600; color: white; }
                .mono-sm { font-family: monospace; color: var(--text-muted); font-size: 0.9rem; }
                
                .role-badge { padding: 4px 10px; border-radius: 20px; font-size: 0.75rem; font-weight: 800; text-transform: uppercase; letter-spacing: 0.5px; }
                .role-badge.admin { background: rgba(255, 82, 82, 0.1); color: var(--danger); border: 1px solid rgba(255, 82, 82, 0.2); }
                .role-badge.operador { background: rgba(0, 229, 255, 0.1); color: var(--secondary); border: 1px solid rgba(0, 229, 255, 0.2); }
                .role-badge.normal { background: rgba(255, 255, 255, 0.1); color: white; border: 1px solid rgba(255, 255, 255, 0.2); }
                
                .loading-row { padding: 40px; display: flex; justify-content: center; }
                .empty-state { padding: 40px; text-align: center; color: var(--text-muted); font-style: italic; }
                
                .success-banner { margin-bottom: 20px; padding: 15px; background: rgba(0, 230, 118, 0.1); border: 1px solid var(--success); color: var(--success); border-radius: 10px; display: flex; align-items: center; gap: 10px; font-weight: 600; }
                
                .admin-form .row-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
                .error-msg { color: var(--danger); font-size: 0.9rem; margin-bottom: 15px; background: rgba(255, 82, 82, 0.1); padding: 10px; border-radius: 6px; }

                .user-details-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
                .detail-item label { display: block; font-size: 0.8rem; color: var(--text-muted); margin-bottom: 5px; }
                .detail-item p { font-size: 1rem; font-weight: 600; }
                .detail-item p { font-size: 1rem; font-weight: 600; }

                @media (max-width: 768px) {
                    .card-header-row { flex-direction: column; align-items: stretch; gap: 15px; }
                    .search-box { width: 100%; }
                    .search-box input { width: 100%; }
                    
                    .table-header { display: none; }
                    .table-row { 
                        grid-template-columns: 1fr; 
                        gap: 10px; 
                        padding: 20px;
                        position: relative;
                    }
                    .user-name { font-size: 1.1rem; margin-bottom: 5px; }
                    .mono-sm { font-size: 0.9rem; opacity: 0.8; }
                    .actions { margin-top: 10px; }
                    .btn-text { 
                        background: rgba(255,255,255,0.1); 
                        padding: 8px 16px; 
                        border-radius: 6px; 
                        width: 100%; 
                        text-align: center;
                    }
                    
                    .admin-form .row-2 { grid-template-columns: 1fr; }
                    .user-details-grid { grid-template-columns: 1fr; }
                }
            `}</style>
        </div>
    );
};

export default AdminDashboard;
