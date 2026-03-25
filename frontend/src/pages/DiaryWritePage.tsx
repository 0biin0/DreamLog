import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link } from 'react-router-dom';
import { useCreateDiary } from '../hooks/useDiaries';
import PromptSuggestion from '../components/prompt/PromptSuggestion';

const diarySchema = z.object({
  title: z.string().min(1, '제목을 입력해주세요.').max(200, '제목은 200자 이하여야 합니다.'),
  content: z.string().min(1, '내용을 입력해주세요.'),
  unlockDate: z.string().min(1, '잠금 해제 날짜를 선택해주세요.'),
});

type DiaryForm = z.infer<typeof diarySchema>;

export default function DiaryWritePage() {
  const createDiary = useCreateDiary();
  const {
    register,
    handleSubmit,
    setValue,
    getValues,
    formState: { errors },
  } = useForm<DiaryForm>({
    resolver: zodResolver(diarySchema),
  });

  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  const minDate = tomorrow.toISOString().split('T')[0];

  const onSubmit = (data: DiaryForm) => {
    createDiary.mutate(data);
  };

  const handlePromptSelect = (prompt: string) => {
    const current = getValues('content');
    if (current) {
      setValue('content', current + '\n\n' + prompt);
    } else {
      setValue('content', prompt);
    }
  };

  return (
    <div className="max-w-2xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-slate-800">미래일기 쓰기</h1>
        <Link to="/diary/list" className="text-sm text-slate-500 hover:text-slate-700">
          목록으로
        </Link>
      </div>

      <div className="space-y-6">
        <PromptSuggestion onSelect={handlePromptSelect} />

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
          <div className="bg-white rounded-2xl shadow-md p-6 space-y-5">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">
                잠금 해제 날짜
              </label>
              <input
                type="date"
                min={minDate}
                {...register('unlockDate')}
                className="w-full px-4 py-3 rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
              />
              {errors.unlockDate && (
                <p className="text-red-500 text-sm mt-1">{errors.unlockDate.message}</p>
              )}
              <p className="text-xs text-slate-400 mt-1">
                이 날짜까지 편지가 잠기며, 해당 날짜에 열립니다.
              </p>
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">
                제목
              </label>
              <input
                type="text"
                {...register('title')}
                className="w-full px-4 py-3 rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                placeholder="미래의 나에게..."
              />
              {errors.title && (
                <p className="text-red-500 text-sm mt-1">{errors.title.message}</p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">
                내용
              </label>
              <textarea
                {...register('content')}
                rows={12}
                className="w-full px-4 py-3 rounded-lg border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent resize-none"
                placeholder="미래의 나에게 편지를 써보세요..."
              />
              {errors.content && (
                <p className="text-red-500 text-sm mt-1">{errors.content.message}</p>
              )}
            </div>
          </div>

          {createDiary.isError && (
            <p className="text-red-500 text-sm text-center">
              저장에 실패했습니다. 다시 시도해주세요.
            </p>
          )}

          <button
            type="submit"
            disabled={createDiary.isPending}
            className="w-full py-4 bg-indigo-600 text-white rounded-2xl font-medium text-lg hover:bg-indigo-700 transition-colors disabled:opacity-50 shadow-lg shadow-indigo-200"
          >
            {createDiary.isPending ? '저장 중...' : '미래에게 보내기'}
          </button>
        </form>
      </div>
    </div>
  );
}
