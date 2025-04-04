import { BrowserRouter, Routes, Route } from "react-router-dom";
import Header from "./components/Header";
import MainPage from "./pages/MainPage";
import SettingPage from "./pages/SettingPage";
import QnaPage from "./pages/QnaPage";

function App() {
  return (
    <BrowserRouter>
      <Header />
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/diary" element={<MainPage />} />
        <Route path="/setting" element={<SettingPage />} />
        <Route path="/qna" element={<QnaPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
