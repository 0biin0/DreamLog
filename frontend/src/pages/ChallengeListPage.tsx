import { Link } from 'react-router-dom';
import { useJoinableChallenges, useMyChallenges } from '../hooks/useChallenges';

export default function ChallengeListPage() {
  const { data: joinable, isLoading: loadingJoinable } = useJoinableChallenges();
  const { data: mine, isLoading: loadingMine } = useMyChallenges();

  const formatDate = (dateStr: string) =>
    new Date(dateStr).toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' });

  const statusLabel = {
    UPCOMING: { text: '시작 전', color: 'bg-blue-100 text-blue-700' },
    ACTIVE: { text: '진행 중', color: 'bg-green-100 text-green-700' },
    ENDED: { text: '종료', color: 'bg-slate-100 text-slate-500' },
  };

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-slate-800">챌린지</h1>
        <Link
          to="/challenges/create"
          className="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 transition-colors"
        >
          챌린지 만들기
        </Link>
      </div>

      {/* 내 챌린지 */}
      <section>
        <h2 className="text-lg font-semibold text-slate-700 mb-4">내 챌린지</h2>
        {loadingMine && <p className="text-slate-400">불러오는 중...</p>}
        {mine && mine.length === 0 && (
          <p className="text-slate-500 text-sm">참가 중인 챌린지가 없습니다.</p>
        )}
        {mine && mine.length > 0 && (
          <div className="space-y-3">
            {mine.map((c) => {
              const badge = statusLabel[c.status];
              return (
                <Link
                  key={c.id}
                  to={`/challenges/${c.id}`}
                  className="block bg-white rounded-xl shadow-sm p-5 hover:shadow-md transition-shadow"
                >
                  <div className="flex items-center justify-between mb-2">
                    <h3 className="font-semibold text-slate-800">{c.title}</h3>
                    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${badge.color}`}>
                      {badge.text}
                    </span>
                  </div>
                  <div className="flex items-center gap-4 text-sm text-slate-500">
                    <span>{formatDate(c.startDate)} ~ {formatDate(c.endDate)}</span>
                    <span>{c.currentParticipants}/{c.maxParticipants}명</span>
                    <span>{c.durationDays}일</span>
                  </div>
                </Link>
              );
            })}
          </div>
        )}
      </section>

      {/* 참가 가능한 챌린지 */}
      <section>
        <h2 className="text-lg font-semibold text-slate-700 mb-4">참가 가능한 챌린지</h2>
        {loadingJoinable && <p className="text-slate-400">불러오는 중...</p>}
        {joinable && joinable.length === 0 && (
          <p className="text-slate-500 text-sm">현재 참가 가능한 챌린지가 없습니다.</p>
        )}
        {joinable && joinable.length > 0 && (
          <div className="space-y-3">
            {joinable.map((c) => (
              <Link
                key={c.id}
                to={`/challenges/${c.id}`}
                className="block bg-white rounded-xl shadow-sm p-5 hover:shadow-md transition-shadow"
              >
                <h3 className="font-semibold text-slate-800 mb-1">{c.title}</h3>
                {c.description && (
                  <p className="text-sm text-slate-500 mb-2 line-clamp-2">{c.description}</p>
                )}
                <div className="flex items-center gap-4 text-sm text-slate-500">
                  <span>시작: {formatDate(c.startDate)}</span>
                  <span>{c.currentParticipants}/{c.maxParticipants}명</span>
                  <span>{c.durationDays}일</span>
                  <span>최대 {c.maxMissedDays}일 불참 가능</span>
                </div>
              </Link>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
