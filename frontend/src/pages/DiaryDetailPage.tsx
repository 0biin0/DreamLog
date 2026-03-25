import { useParams, Link, useNavigate } from 'react-router-dom';
import { useDiary, useDeleteDiary } from '../hooks/useDiaries';
import LockedDiary from '../components/diary/LockedDiary';

export default function DiaryDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { data: diary, isLoading } = useDiary(Number(id));
  const deleteDiary = useDeleteDiary();

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' });
  };

  const handleDelete = () => {
    if (window.confirm('정말 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
      deleteDiary.mutate(Number(id));
    }
  };

  if (isLoading) {
    return <div className="text-center py-16 text-slate-400">불러오는 중...</div>;
  }

  if (!diary) {
    return (
      <div className="text-center py-16">
        <p className="text-slate-500">일기를 찾을 수 없습니다.</p>
        <Link to="/diary/list" className="text-indigo-600 hover:underline mt-2 inline-block">
          목록으로 돌아가기
        </Link>
      </div>
    );
  }

  if (diary.locked) {
    return (
      <div>
        <div className="mb-6">
          <Link to="/diary/list" className="text-sm text-slate-500 hover:text-slate-700">
            &larr; 목록으로
          </Link>
        </div>
        <div className="bg-white rounded-2xl shadow-md p-8">
          <LockedDiary diary={diary} />
        </div>
        <div className="flex justify-center mt-4">
          <button
            onClick={handleDelete}
            className="text-sm text-red-400 hover:text-red-600 transition-colors"
          >
            삭제
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto">
      <div className="mb-6">
        <Link to="/diary/list" className="text-sm text-slate-500 hover:text-slate-700">
          &larr; 목록으로
        </Link>
      </div>

      <div className="bg-white rounded-2xl shadow-md p-8">
        <div className="flex items-start justify-between mb-4">
          <div>
            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-700 mb-2">
              &#128275; 열린 편지
            </span>
            <h1 className="text-2xl font-bold text-slate-800">{diary.title}</h1>
          </div>
        </div>

        <div className="flex items-center gap-4 text-sm text-slate-500 mb-8 pb-6 border-b border-slate-100">
          <span>작성일: {formatDate(diary.writtenDate)}</span>
          <span>해제일: {formatDate(diary.unlockDate)}</span>
        </div>

        <div className="prose prose-slate max-w-none whitespace-pre-wrap text-slate-700 leading-relaxed">
          {diary.content}
        </div>
      </div>

      <div className="flex items-center justify-between mt-6">
        {diary.editable && (
          <button
            onClick={() => navigate(`/diary/${diary.id}/edit`)}
            className="px-4 py-2 text-sm text-indigo-600 border border-indigo-200 rounded-lg hover:bg-indigo-50 transition-colors"
          >
            수정
          </button>
        )}
        <button
          onClick={handleDelete}
          className="text-sm text-red-400 hover:text-red-600 transition-colors ml-auto"
        >
          삭제
        </button>
      </div>
    </div>
  );
}
