package com.swe.ux.views;

import com.swe.ux.viewmodels.DashboardViewModel;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

class DashboardView extends VBox {
    
    private Label usersPresentValueLabel;
    private Label usersLoggedOutValueLabel;
    private Label meetingSummaryValueLabel;
    
    public DashboardView() {
        setPadding(new Insets(30));
        setSpacing(20);
        setAlignment(Pos.TOP_LEFT);
        setStyle("-fx-background-color: #f5f5f5;");
        
        // Title
        Label titleLabel = new Label("Meet Analytics Dashboard");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web("#333333"));
        
        // Users Present Section (Card)
        VBox usersPresentCard = createCard();
        Label usersPresentLabel = new Label("Users Present");
        usersPresentLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        usersPresentLabel.setTextFill(Color.web("#666666"));
        
        usersPresentValueLabel = new Label();
        usersPresentValueLabel.setFont(Font.font("System", FontWeight.BOLD, 36));
        usersPresentValueLabel.setTextFill(Color.web("#2196F3"));
        
        usersPresentCard.getChildren().addAll(usersPresentLabel, usersPresentValueLabel);
        
        // Users Logged Out Section (Card)
        VBox usersLoggedOutCard = createCard();
        Label usersLoggedOutLabel = new Label("Users Logged Out");
        usersLoggedOutLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        usersLoggedOutLabel.setTextFill(Color.web("#666666"));
        
        usersLoggedOutValueLabel = new Label();
        usersLoggedOutValueLabel.setFont(Font.font("System", FontWeight.BOLD, 36));
        usersLoggedOutValueLabel.setTextFill(Color.web("#F44336"));
        
        usersLoggedOutCard.getChildren().addAll(usersLoggedOutLabel, usersLoggedOutValueLabel);
        
        // Previous Meeting Summary Section (Card)
        VBox meetingSummaryCard = createCard();
        meetingSummaryCard.setPrefHeight(120);
        Label meetingSummaryLabel = new Label("Previous Meeting Summary");
        meetingSummaryLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
        meetingSummaryLabel.setTextFill(Color.web("#666666"));
        
        meetingSummaryValueLabel = new Label();
        meetingSummaryValueLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        meetingSummaryValueLabel.setTextFill(Color.web("#757575"));
        meetingSummaryValueLabel.setWrapText(true);
        meetingSummaryValueLabel.setMaxWidth(520);
        
        meetingSummaryCard.getChildren().addAll(meetingSummaryLabel, meetingSummaryValueLabel);
        
        getChildren().addAll(
            titleLabel,
            usersPresentCard,
            usersLoggedOutCard,
            meetingSummaryCard
        );
    }
    
    private VBox createCard() {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; " +
                     "-fx-background-radius: 8; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setAlignment(Pos.TOP_LEFT);
        return card;
    }
    
    public void setViewModel(DashboardViewModel viewModel) {
        usersPresentValueLabel.textProperty().bind(
            viewModel.usersPresentProperty().asString()
        );
        usersLoggedOutValueLabel.textProperty().bind(
            viewModel.usersLoggedOutProperty().asString()
        );
        meetingSummaryValueLabel.textProperty().bind(
            viewModel.meetingSummaryProperty()
        );
    }
}