import { Link } from 'react-router-dom';
import type { DiaryResponse } from '../../types/diary';

interface Props {
  diary: DiaryResponse;
}

export default function DiaryCard({ diary }: Props) {
  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' });
  };

  return (
    <Link
      to={`/diary/${diary.id}`}
      className="block bg-white rounded-2xl shadow-md p-6 hover:shadow-lg transition-shadow"
    >
      <div className="flex items-start justify-between mb-3">
        <h3 className="text-lg font-semibold text-slate-800 truncate flex-1">
          {diary.title}
        </h3>
        {diary.locked ? (
          <span className="ml-3 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-amber-100 text-amber-700">
            &#128274; D-{diary.daysUntilUnlock}
          </span>
        ) : (
          <span className="ml-3 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-700">
            &#128275; 열림
          </span>
        )}
      </div>

      <div className="flex items-center gap-4 text-sm text-slate-500">
        <span>작성: {formatDate(diary.writtenDate)}</span>
        <span>해제: {formatDate(diary.unlockDate)}</span>
      </div>

      {diary.editable && (
        <p className="mt-2 text-xs text-indigo-500">수정 가능</p>
      )}
    </Link>
  );
}
