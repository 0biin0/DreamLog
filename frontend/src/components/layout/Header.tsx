import { Link } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { useLogout } from '../../hooks/useAuth';

export default function Header() {
  const user = useAuthStore((s) => s.user);
  const logout = useLogout();

  return (
    <header className="bg-white border-b border-slate-200">
      <div className="max-w-5xl mx-auto px-4 h-16 flex items-center justify-between">
        <div className="flex items-center gap-6">
          <Link to="/" className="text-xl font-bold text-indigo-600">
            DreamLog
          </Link>
          {user && (
            <nav className="flex items-center gap-4">
              <Link to="/diary/list" className="text-sm text-slate-600 hover:text-indigo-600 transition-colors">
                미래일기
              </Link>
              <Link to="/challenges" className="text-sm text-slate-600 hover:text-indigo-600 transition-colors">
                챌린지
              </Link>
            </nav>
          )}
        </div>

        {user && (
          <div className="flex items-center gap-4">
            <span className="text-sm text-slate-600">
              {user.nickname}
            </span>
            <button
              onClick={() => logout.mutate()}
              className="text-sm text-slate-400 hover:text-slate-600 transition-colors"
            >
              로그아웃
            </button>
          </div>
        )}
      </div>
    </header>
  );
}
