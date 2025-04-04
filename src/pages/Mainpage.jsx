import '../css/mainpage.css';

function MainPage() {
    return (
      <div className="main-container">
        <div className="top-section">
          <div className="block">
            <div className="block-title">Manifestation Diary</div>
            <img src="/img/diary.png" alt="Manifestation Diary" />
          </div>
          <div className="block">
            <div className="block-text">
              생각은 현실이 된다,<br />
              기록에서 시작하세요.
            </div>
          </div>
        </div>
  
        <div className="bottom-section">
          <div className="block">
            <div className="block-text">
              당신의 내일을, 오늘 여기 적어보세요.
            </div>
          </div>
          <div className="block">
            <img src="/img/write.png" alt="Checklist" />
          </div>
        </div>
      </div>
    );
  }
  
  export default MainPage;
  