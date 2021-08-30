import React from "react";

import { withRouter } from 'react-router-dom';

class TurnConfirmation extends React.Component {

    constructor(props) {
        super(props);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleSubmit(event) {
        event.preventDefault();
        this.props.history.push("/turns");
    }

    render() {
        const turnNumber = this.props.location.state.turnNumber;

        return (
            <main className="form-signin">
                <form onSubmit={this.handleSubmit}>
                    <h1 className="h3 mb-3 fw-normal">Confirmation</h1>

                    <p>
                        Your turn is number {turnNumber}, we will send you a SMS when you can come back.
                    </p>

                    <button className="w-100 btn btn-lg btn-primary" type="submit" autoFocus>Ok</button>
                </form>
            </main>
        );
    }
}

export default withRouter(TurnConfirmation);