import type { DiaryResponse } from '../../types/diary';

interface Props {
  diary: DiaryResponse;
}

export default function LockedDiary({ diary }: Props) {
  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' });
  };

  return (
    <div className="text-center py-16">
      <div className="w-24 h-24 bg-amber-100 rounded-full flex items-center justify-center mx-auto mb-6">
        <span className="text-5xl">&#128274;</span>
      </div>

      <h2 className="text-2xl font-bold text-slate-800 mb-2">{diary.title}</h2>

      <p className="text-slate-500 mb-6">
        이 편지는 아직 잠겨 있습니다.
      </p>

      <div className="inline-flex items-center gap-2 bg-amber-50 rounded-xl px-6 py-4">
        <span className="text-4xl font-bold text-amber-600">D-{diary.daysUntilUnlock}</span>
      </div>

      <p className="text-sm text-slate-400 mt-4">
        {formatDate(diary.unlockDate)}에 열립니다
      </p>
    </div>
  );
}
