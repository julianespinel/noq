import {BrowserRouter as Router, Route, Switch} from "react-router-dom";
import './App.css';
import CompanyRegistration from "./company/CompanyRegistration";
import BranchRegistration from "./branch/BranchRegistration";
import QueueRegistration from "./queue/QueueRegistration";

function App() {
    return (
        <Router>
            <Switch>
                <Route path="/companies">
                    <CompanyRegistration/>
                </Route>
                <Route path="/branches">
                    <BranchRegistration/>
                </Route>
                <Route path="/queues">
                    <QueueRegistration/>
                </Route>
            </Switch>
        </Router>
    );
}

export default App;
