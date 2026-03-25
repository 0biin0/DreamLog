import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useDiaryList } from '../hooks/useDiaries';
import DiaryCard from '../components/diary/DiaryCard';

export default function DiaryListPage() {
  const [page, setPage] = useState(0);
  const { data, isLoading } = useDiaryList(page);

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-slate-800">내 미래일기</h1>
        <Link
          to="/diary/write"
          className="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 transition-colors"
        >
          새 일기 쓰기
        </Link>
      </div>

      {isLoading && (
        <div className="text-center py-16 text-slate-400">불러오는 중...</div>
      )}

      {data && data.content.length === 0 && (
        <div className="text-center py-16">
          <div className="w-20 h-20 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <span className="text-3xl">&#9993;</span>
          </div>
          <p className="text-slate-500 mb-4">아직 작성한 미래일기가 없습니다.</p>
          <Link
            to="/diary/write"
            className="text-indigo-600 font-medium hover:underline"
          >
            첫 번째 편지를 써보세요
          </Link>
        </div>
      )}

      {data && data.content.length > 0 && (
        <>
          <div className="space-y-4">
            {data.content.map((diary) => (
              <DiaryCard key={diary.id} diary={diary} />
            ))}
          </div>

          {data.totalPages > 1 && (
            <div className="flex justify-center gap-2 mt-8">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                className="px-4 py-2 rounded-lg border border-slate-300 text-sm disabled:opacity-30 hover:bg-slate-50"
              >
                이전
              </button>
              <span className="px-4 py-2 text-sm text-slate-600">
                {page + 1} / {data.totalPages}
              </span>
              <button
                onClick={() => setPage((p) => p + 1)}
                disabled={page >= data.totalPages - 1}
                className="px-4 py-2 rounded-lg border border-slate-300 text-sm disabled:opacity-30 hover:bg-slate-50"
              >
                다음
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
