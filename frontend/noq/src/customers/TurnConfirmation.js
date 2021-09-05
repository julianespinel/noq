import React from "react";

import { withRouter } from 'react-router-dom';

class TurnConfirmation extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            turnNumber: props.location.state.turnNumber
        }

        this.handleSubmit = this.handleSubmit.bind(this);
    }

    async handleSubmit(event) {
        event.preventDefault();
        await this.props.history.push("/turns");
    }

    render() {
        return (
            <main className="form-signin">
                <form onSubmit={this.handleSubmit}>
                    <h1 className="h3 mb-3 fw-normal">Confirmation</h1>

                    <p>
                        Your turn is number {this.state.turnNumber}, we will send you a SMS when you can come back.
                    </p>

                    <button className="w-100 btn btn-lg btn-primary" type="submit" autoFocus>Ok</button>
                </form>
            </main>
        );
    }
}

export default withRouter(TurnConfirmation);