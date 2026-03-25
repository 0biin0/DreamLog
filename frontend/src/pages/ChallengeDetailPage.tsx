import { useParams, Link } from 'react-router-dom';
import { useChallenge, useChallengeProgress, useJoinChallenge } from '../hooks/useChallenges';
import { useAuthStore } from '../store/authStore';
import ProgressBar from '../components/challenge/ProgressBar';
import DailyCheckGrid from '../components/challenge/DailyCheckGrid';

export default function ChallengeDetailPage() {
  const { id } = useParams<{ id: string }>();
  const challengeId = Number(id);
  const user = useAuthStore((s) => s.user);
  const { data: challenge, isLoading } = useChallenge(challengeId);
  const { data: progress } = useChallengeProgress(challengeId);
  const joinChallenge = useJoinChallenge();

  const formatDate = (dateStr: string) =>
    new Date(dateStr).toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' });

  const statusStyle = {
    UPCOMING: { text: '시작 전', bg: 'bg-blue-100 text-blue-700' },
    ACTIVE: { text: '진행 중', bg: 'bg-green-100 text-green-700' },
    ENDED: { text: '종료', bg: 'bg-slate-100 text-slate-500' },
  };

  const participantStatusStyle = {
    ACTIVE: { text: '진행 중', color: 'text-indigo-600' },
    COMPLETED: { text: '완주 성공!', color: 'text-green-600' },
    FAILED: { text: '실패', color: 'text-red-600' },
  };

  if (isLoading) return <div className="text-center py-16 text-slate-400">불러오는 중...</div>;
  if (!challenge) return <div className="text-center py-16 text-slate-500">챌린지를 찾을 수 없습니다.</div>;

  const badge = statusStyle[challenge.status];
  const isParticipant = !!progress;

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <Link to="/challenges" className="text-sm text-slate-500 hover:text-slate-700">
        &larr; 챌린지 목록
      </Link>

      <div className="bg-white rounded-2xl shadow-md p-6">
        <div className="flex items-center justify-between mb-4">
          <h1 className="text-2xl font-bold text-slate-800">{challenge.title}</h1>
          <span className={`px-3 py-1 rounded-full text-xs font-medium ${badge.bg}`}>
            {badge.text}
          </span>
        </div>

        {challenge.description && (
          <p className="text-slate-600 mb-6">{challenge.description}</p>
        )}

        <div className="grid grid-cols-2 gap-4 text-sm">
          <div className="bg-slate-50 rounded-lg p-3">
            <p className="text-slate-400">기간</p>
            <p className="font-medium text-slate-700">{challenge.durationDays}일</p>
          </div>
          <div className="bg-slate-50 rounded-lg p-3">
            <p className="text-slate-400">참가자</p>
            <p className="font-medium text-slate-700">{challenge.currentParticipants}/{challenge.maxParticipants}명</p>
          </div>
          <div className="bg-slate-50 rounded-lg p-3">
            <p className="text-slate-400">시작일</p>
            <p className="font-medium text-slate-700">{formatDate(challenge.startDate)}</p>
          </div>
          <div className="bg-slate-50 rounded-lg p-3">
            <p className="text-slate-400">종료일</p>
            <p className="font-medium text-slate-700">{formatDate(challenge.endDate)}</p>
          </div>
        </div>

        <div className="mt-4 bg-amber-50 rounded-lg p-3 text-sm text-amber-700">
          최대 {challenge.maxMissedDays}일까지 불참 가능 (총 {challenge.durationDays}일 중 15% 초과 시 실패)
        </div>
      </div>

      {/* 내 진행 상황 */}
      {isParticipant && progress && (
        <div className="bg-white rounded-2xl shadow-md p-6">
          <h2 className="text-lg font-semibold text-slate-800 mb-4">내 진행 상황</h2>

          <div className="flex items-center justify-between mb-3">
            <span className={`font-semibold ${participantStatusStyle[progress.status].color}`}>
              {participantStatusStyle[progress.status].text}
            </span>
            <span className="text-sm text-slate-500">
              {progress.achievedDays}/{progress.totalDays}일 달성
            </span>
          </div>

          <ProgressBar percent={progress.progressPercent} status={progress.status} />

          <div className="flex items-center gap-6 mt-4 text-sm text-slate-600">
            <span>불참: {progress.missedCount}/{progress.maxMissedDays}일</span>
            <span>진행률: {progress.progressPercent}%</span>
          </div>

          {progress.dailyLogs.length > 0 && (
            <div className="mt-6">
              <h3 className="text-sm font-medium text-slate-700 mb-3">일별 기록</h3>
              <DailyCheckGrid logs={progress.dailyLogs} />
            </div>
          )}
        </div>
      )}

      {/* 참가 버튼 */}
      {!isParticipant && challenge.status === 'UPCOMING' && (
        <button
          onClick={() => joinChallenge.mutate(challengeId)}
          disabled={joinChallenge.isPending}
          className="w-full py-4 bg-indigo-600 text-white rounded-2xl font-medium text-lg hover:bg-indigo-700 transition-colors disabled:opacity-50"
        >
          {joinChallenge.isPending ? '참가 중...' : '챌린지 참가하기'}
        </button>
      )}

      {joinChallenge.isError && (
        <p className="text-red-500 text-sm text-center">참가에 실패했습니다.</p>
      )}

      {joinChallenge.isSuccess && (
        <p className="text-green-600 text-sm text-center font-medium">참가가 완료되었습니다!</p>
      )}
    </div>
  );
}
