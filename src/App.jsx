import { BrowserRouter, Routes, Route } from "react-router-dom";
import Header from "./components/Header";
import MainPage from "./pages/MainPage";
import SettingPage from "./pages/SettingPage";
import QnaPage from "./pages/QnaPage";
import Diarypage from "./pages/diarypage";
import LoginPage from "./pages/loginPage";
import Footer from "./components/Footer";

function App() {
  return (
    <div className="app-wrapper">
      <BrowserRouter>
        <Header />
        <div className="main-body">
          <Routes>
            <Route path="/" element={<MainPage />} />
            <Route path="/diary" element={<Diarypage />} />
            <Route path="/setting" element={<SettingPage />} />
            <Route path="/qna" element={<QnaPage />} />
            <Route path="/login" element={<LoginPage />} />
          </Routes>
        </div>
        <Footer />
      </BrowserRouter>
    </div>
  );
}

export default App;
