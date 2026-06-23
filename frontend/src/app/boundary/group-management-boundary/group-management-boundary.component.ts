import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { GroupService } from '../../services/group.service';

@Component({
  selector: 'app-group-management-boundary',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './group-management-boundary.component.html',
  styleUrl: './group-management-boundary.component.css'
})
export class GroupManagementBoundaryComponent implements OnInit {
  groups: any[] = [];
  newGroupName: string = '';
  errorMessage: string = '';
  successMessage: string = '';

  constructor(private groupService: GroupService) {}

  ngOnInit() {
    this.loadGroups();
  }

  loadGroups() {
    this.groupService.getGroups().subscribe({
      next: (data) => this.groups = data,
      error: () => this.errorMessage = 'Errore nel caricamento dei gruppi.'
    });
  }

  createGroup() {
    if (!this.newGroupName || this.newGroupName.trim().length === 0) return;
    this.groupService.createGroup(this.newGroupName).subscribe({
      next: (res) => {
        this.successMessage = 'Gruppo creato con successo!';
        this.newGroupName = '';
        this.loadGroups();
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: () => this.errorMessage = 'Errore durante la creazione del gruppo.'
    });
  }

  deleteGroup(id: number) {
    if (confirm('Sei sicuro di voler eliminare questo gruppo? I contenuti all\'interno non verranno cancellati, ma la cartella andrà persa.')) {
      this.groupService.deleteGroup(id).subscribe({
        next: () => this.loadGroups(),
        error: () => this.errorMessage = 'Impossibile eliminare il gruppo.'
      });
    }
  }
}
