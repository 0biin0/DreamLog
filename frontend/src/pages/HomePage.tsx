import { Link } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { useArrivedDiaries } from '../hooks/useDiaries';
import { useMyChallenges } from '../hooks/useChallenges';

export default function HomePage() {
  const user = useAuthStore((s) => s.user);
  const { data: arrivedDiaries } = useArrivedDiaries();
  const { data: myChallenges } = useMyChallenges();

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('ko-KR', { month: 'long', day: 'numeric' });
  };

  return (
    <div className="space-y-8">
      <div>
        <h2 className="text-2xl font-bold text-slate-800">
          {user?.nickname}님, 안녕하세요!
        </h2>
        <p className="text-slate-500 mt-1">오늘도 미래의 나에게 편지를 보내보세요.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-white rounded-2xl shadow-md p-6">
          <div className="flex items-center gap-3 mb-4">
            <div className="w-10 h-10 bg-amber-100 rounded-full flex items-center justify-center">
              <span className="text-amber-600 text-lg">&#9993;</span>
            </div>
            <h3 className="text-lg font-semibold text-slate-800">도착한 편지</h3>
          </div>

          {arrivedDiaries && arrivedDiaries.length > 0 ? (
            <ul className="space-y-2">
              {arrivedDiaries.slice(0, 5).map((diary) => (
                <li key={diary.id}>
                  <Link
                    to={`/diary/${diary.id}`}
                    className="flex items-center justify-between p-3 rounded-lg hover:bg-amber-50 transition-colors"
                  >
                    <span className="text-sm text-slate-700 truncate">{diary.title}</span>
                    <span className="text-xs text-slate-400 ml-2 shrink-0">
                      {formatDate(diary.unlockDate)}
                    </span>
                  </Link>
                </li>
              ))}
              {arrivedDiaries.length > 5 && (
                <li className="text-center">
                  <Link to="/diary/list" className="text-sm text-indigo-600 hover:underline">
                    +{arrivedDiaries.length - 5}개 더 보기
                  </Link>
                </li>
              )}
            </ul>
          ) : (
            <div>
              <p className="text-slate-500 text-sm mb-2">아직 도착한 편지가 없습니다.</p>
              <p className="text-xs text-slate-400">
                미래 일기를 작성하면, 지정한 날짜에 편지가 도착합니다.
              </p>
            </div>
          )}
        </div>

        <div className="bg-white rounded-2xl shadow-md p-6">
          <div className="flex items-center gap-3 mb-4">
            <div className="w-10 h-10 bg-indigo-100 rounded-full flex items-center justify-center">
              <span className="text-indigo-600 text-lg">&#9733;</span>
            </div>
            <h3 className="text-lg font-semibold text-slate-800">참가 중인 챌린지</h3>
          </div>

          {myChallenges && myChallenges.length > 0 ? (
            <ul className="space-y-2">
              {myChallenges.slice(0, 3).map((c) => (
                <li key={c.id}>
                  <Link
                    to={`/challenges/${c.id}`}
                    className="flex items-center justify-between p-3 rounded-lg hover:bg-indigo-50 transition-colors"
                  >
                    <span className="text-sm text-slate-700 truncate">{c.title}</span>
                    <span className={`text-xs px-2 py-0.5 rounded-full ${
                      c.status === 'ACTIVE' ? 'bg-green-100 text-green-700' :
                      c.status === 'UPCOMING' ? 'bg-blue-100 text-blue-700' :
                      'bg-slate-100 text-slate-500'
                    }`}>
                      {c.status === 'ACTIVE' ? '진행 중' : c.status === 'UPCOMING' ? '시작 전' : '종료'}
                    </span>
                  </Link>
                </li>
              ))}
              {myChallenges.length > 3 && (
                <li className="text-center">
                  <Link to="/challenges" className="text-sm text-indigo-600 hover:underline">
                    +{myChallenges.length - 3}개 더 보기
                  </Link>
                </li>
              )}
            </ul>
          ) : (
            <div>
              <p className="text-slate-500 text-sm mb-2">참가 중인 챌린지가 없습니다.</p>
              <p className="text-xs text-slate-400">
                챌린지에 참가해서 꾸준한 기록 습관을 만들어보세요.
              </p>
            </div>
          )}
        </div>
      </div>

      <div className="flex justify-center gap-4">
        <Link
          to="/diary/write"
          className="inline-flex items-center gap-2 px-8 py-4 bg-indigo-600 text-white rounded-2xl font-medium text-lg hover:bg-indigo-700 transition-colors shadow-lg shadow-indigo-200"
        >
          <span>&#9998;</span>
          새 미래일기 쓰기
        </Link>
        <Link
          to="/diary/list"
          className="inline-flex items-center gap-2 px-6 py-4 border border-slate-300 text-slate-600 rounded-2xl font-medium hover:bg-slate-50 transition-colors"
        >
          내 일기 보기
        </Link>
      </div>
    </div>
  );
}
