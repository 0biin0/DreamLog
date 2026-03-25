import { useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { promptApi } from '../../api/promptApi';

interface Props {
  onSelect: (prompt: string) => void;
}

const categories = [
  { value: '', label: '전체' },
  { value: 'MOTIVATION', label: '동기부여' },
  { value: 'REFLECTION', label: '성찰' },
  { value: 'GRATITUDE', label: '감사' },
  { value: 'DREAM', label: '꿈' },
  { value: 'RANDOM', label: '랜덤' },
];

export default function PromptSuggestion({ onSelect }: Props) {
  const [category, setCategory] = useState('');
  const [showAi, setShowAi] = useState(false);
  const [goal, setGoal] = useState('');
  const [mood, setMood] = useState('');

  const { data: randomPrompt, refetch } = useQuery({
    queryKey: ['prompt', 'random', category],
    queryFn: () => promptApi.getRandom(category || undefined).then((res) => res.data.data!),
  });

  const aiMutation = useMutation({
    mutationFn: () => promptApi.getAi({ goal: goal || undefined, mood: mood || undefined }),
  });

  const handleAiGenerate = () => {
    aiMutation.mutate(undefined, {
      onSuccess: (res) => {
        if (res.data.data) {
          onSelect(res.data.data.prompt);
        }
      },
    });
  };

  return (
    <div className="bg-amber-50 rounded-xl p-5 space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-semibold text-amber-800">작성 도우미</h3>
        <div className="flex gap-1">
          <button
            onClick={() => setShowAi(false)}
            className={`px-3 py-1 rounded-md text-xs font-medium transition-colors ${
              !showAi ? 'bg-amber-200 text-amber-800' : 'text-amber-600 hover:bg-amber-100'
            }`}
          >
            프롬프트
          </button>
          <button
            onClick={() => setShowAi(true)}
            className={`px-3 py-1 rounded-md text-xs font-medium transition-colors ${
              showAi ? 'bg-amber-200 text-amber-800' : 'text-amber-600 hover:bg-amber-100'
            }`}
          >
            AI 개인화
          </button>
        </div>
      </div>

      {!showAi ? (
        <>
          <div className="flex gap-1.5 flex-wrap">
            {categories.map((cat) => (
              <button
                key={cat.value}
                onClick={() => setCategory(cat.value)}
                className={`px-2.5 py-1 rounded-md text-xs transition-colors ${
                  category === cat.value
                    ? 'bg-amber-600 text-white'
                    : 'bg-white text-amber-700 hover:bg-amber-100'
                }`}
              >
                {cat.label}
              </button>
            ))}
          </div>

          {randomPrompt && (
            <div className="bg-white rounded-lg p-4">
              <p className="text-sm text-slate-700 leading-relaxed">{randomPrompt.content}</p>
            </div>
          )}

          <div className="flex gap-2">
            <button
              onClick={() => refetch()}
              className="px-3 py-1.5 bg-white text-amber-700 rounded-md text-xs font-medium hover:bg-amber-100 transition-colors"
            >
              다른 프롬프트
            </button>
            {randomPrompt && (
              <button
                onClick={() => onSelect(randomPrompt.content)}
                className="px-3 py-1.5 bg-amber-600 text-white rounded-md text-xs font-medium hover:bg-amber-700 transition-colors"
              >
                이 주제로 쓰기
              </button>
            )}
          </div>
        </>
      ) : (
        <>
          <div className="space-y-3">
            <input
              type="text"
              value={goal}
              onChange={(e) => setGoal(e.target.value)}
              placeholder="이루고 싶은 목표 (선택)"
              className="w-full px-3 py-2 rounded-lg border border-amber-200 text-sm focus:outline-none focus:ring-2 focus:ring-amber-400"
            />
            <input
              type="text"
              value={mood}
              onChange={(e) => setMood(e.target.value)}
              placeholder="지금 기분 (선택)"
              className="w-full px-3 py-2 rounded-lg border border-amber-200 text-sm focus:outline-none focus:ring-2 focus:ring-amber-400"
            />
          </div>

          {aiMutation.data?.data?.data && (
            <div className="bg-white rounded-lg p-4">
              <p className="text-sm text-slate-700 leading-relaxed">{aiMutation.data.data.data.prompt}</p>
            </div>
          )}

          <button
            onClick={handleAiGenerate}
            disabled={aiMutation.isPending}
            className="px-4 py-2 bg-amber-600 text-white rounded-md text-xs font-medium hover:bg-amber-700 transition-colors disabled:opacity-50"
          >
            {aiMutation.isPending ? 'AI 생성 중...' : 'AI 프롬프트 생성'}
          </button>
        </>
      )}
    </div>
  );
}
