import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import HomePage from './pages/HomePage';
import DiaryWritePage from './pages/DiaryWritePage';
import DiaryListPage from './pages/DiaryListPage';
import DiaryDetailPage from './pages/DiaryDetailPage';
import ChallengeListPage from './pages/ChallengeListPage';
import ChallengeCreatePage from './pages/ChallengeCreatePage';
import ChallengeDetailPage from './pages/ChallengeDetailPage';
import Layout from './components/layout/Layout';
import ProtectedRoute from './components/common/ProtectedRoute';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route element={<ProtectedRoute />}>
            <Route element={<Layout />}>
              <Route path="/" element={<HomePage />} />
              <Route path="/diary/write" element={<DiaryWritePage />} />
              <Route path="/diary/list" element={<DiaryListPage />} />
              <Route path="/diary/:id" element={<DiaryDetailPage />} />
              <Route path="/challenges" element={<ChallengeListPage />} />
              <Route path="/challenges/create" element={<ChallengeCreatePage />} />
              <Route path="/challenges/:id" element={<ChallengeDetailPage />} />
            </Route>
          </Route>
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  );
}
