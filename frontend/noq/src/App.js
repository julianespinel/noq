import { BrowserRouter as Router, Route, Switch } from "react-router-dom";
import './App.css';

import MenuBar from "./common/MenuBar"

import Login from "./common/Login";
import CompanyRegistration from "./company/CompanyRegistration";
import BranchRegistration from "./branch/BranchRegistration";
import QueueRegistration from "./queue/QueueRegistration";

import CallNextTurn from "./agents/CallNextTurn";
import AttendTurn from "./agents/AttendTurn";

import TurnRequest from "./customers/TurnRequest";
import TurnConfirmation from "./customers/TurnConfirmation";
import PrivateRoute from "./common/PrivateRoute";
import Logout from "./common/Logout";

import 'react-toastify/dist/ReactToastify.css';
import { ToastContainer } from 'react-toastify';


function App() {
    return (
        <div className="container-fluid">
            <Router>
                <MenuBar />
                <Switch>
                    <Route path="/login">
                        <Login />
                    </Route>

                    <PrivateRoute path="/companies" roles={["admin"]} component={CompanyRegistration} />
                    <PrivateRoute path="/branches" roles={["admin", "manager"]} component={BranchRegistration} />
                    <PrivateRoute path="/queues" roles={["admin", "manager"]} component={QueueRegistration} />

                    {/* Agent */}

                    <PrivateRoute exact path="/agent" roles={["manager", "agent"]} component={CallNextTurn} />
                    <PrivateRoute path="/agent/turns/:turnNumber" roles={["manager", "agent"]} component={AttendTurn} />

                    {/* Customer */}

                    <PrivateRoute exact path="/turns" roles={["customer"]} component={TurnRequest} />
                    <PrivateRoute path="/turns/:turnNumber/confirmation" roles={["customer"]} component={TurnConfirmation} />

                    <Route path="/logout">
                        <Logout />
                    </Route>
                    {/* Default path */}
                    <Route path="/">
                        <Login />
                    </Route>
                </Switch>
            </Router>
            <ToastContainer
                position="bottom-right"
                autoClose={5000}
                hideProgressBar={false}
                newestOnTop={false}
                closeOnClick
                rtl={false}
                pauseOnFocusLoss
                draggable
                pauseOnHover
            />
        </div>
    );
}

export default App;
