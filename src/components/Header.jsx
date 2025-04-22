import { FaUserCircle } from 'react-icons/fa';
import { useNavigate } from 'react-router-dom';
import '../css/Header.css';

function Header() {
  const navigate = useNavigate();

  return (
    <header className="header">
      <div className="left">
        <span className="logo" onClick={() => navigate('/')}>DreamLog</span>
      </div>
      <nav className="center">
        <span className="nav-item" onClick={() => navigate('/diary')}>Diary</span>
        <span className="nav-item" onClick={() => navigate('/setting')}>Setting</span>
        <span className="nav-item" onClick={() => navigate('/qna')}>From DreamLog</span>
      </nav>
      <div className="right">
        <FaUserCircle className="icon" />
        <span className="login-text" onClick={() => navigate('/login')}>Login</span>
      </div>
    </header>
  );
}

export default Header;
